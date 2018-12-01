package za.co.no9.sle.transform.typelessPatternToTypedPattern

import za.co.no9.sle.*
import za.co.no9.sle.ast.typedPattern.*
import za.co.no9.sle.ast.typedPattern.CallExpression
import za.co.no9.sle.ast.typedPattern.CaseExpression
import za.co.no9.sle.ast.typedPattern.CaseItem
import za.co.no9.sle.ast.typedPattern.ConstantBool
import za.co.no9.sle.ast.typedPattern.ConstantBoolPattern
import za.co.no9.sle.ast.typedPattern.ConstantInt
import za.co.no9.sle.ast.typedPattern.ConstantIntPattern
import za.co.no9.sle.ast.typedPattern.ConstantString
import za.co.no9.sle.ast.typedPattern.ConstantStringPattern
import za.co.no9.sle.ast.typedPattern.ConstantUnitPattern
import za.co.no9.sle.ast.typedPattern.Constructor
import za.co.no9.sle.ast.typedPattern.ConstructorReferencePattern
import za.co.no9.sle.ast.typedPattern.Expression
import za.co.no9.sle.ast.typedPattern.ID
import za.co.no9.sle.ast.typedPattern.IdReference
import za.co.no9.sle.ast.typedPattern.IdReferencePattern
import za.co.no9.sle.ast.typedPattern.IfExpression
import za.co.no9.sle.ast.typedPattern.LambdaExpression
import za.co.no9.sle.ast.typedPattern.LetDeclaration
import za.co.no9.sle.ast.typedPattern.Module
import za.co.no9.sle.ast.typedPattern.Pattern
import za.co.no9.sle.ast.typedPattern.QualifiedID
import za.co.no9.sle.ast.typedPattern.TypeAliasDeclaration
import za.co.no9.sle.ast.typedPattern.Unit
import za.co.no9.sle.ast.typelessPattern.*
import za.co.no9.sle.ast.typelessPattern.Declaration
import za.co.no9.sle.ast.typelessPattern.TypeDeclaration
import za.co.no9.sle.repository.Item
import za.co.no9.sle.repository.Repository
import za.co.no9.sle.typing.*
import java.io.File


data class InferResult(val module: Module, val constaints: Constraints, val environment: Environment)


fun infer(repository: Repository<Item>, sourceFile: File, varPump: VarPump, module: za.co.no9.sle.ast.typelessPattern.Module, env: Environment): Either<Errors, InferResult> {
    val context =
            InferContext(repository, sourceFile, varPump, env)

    val m =
            context.infer(module)

    return if (context.errors.isEmpty())
        value(InferResult(m, context.constraints, context.env))
    else
        error(context.errors)
}


private class InferContext(private val repository: Repository<Item>, private val sourceFile: File, private val varPump: VarPump, internal var env: Environment) {
    var constraints =
            Constraints()

    val errors =
            mutableSetOf<Error>()


    fun infer(module: za.co.no9.sle.ast.typelessPattern.Module): Module {
        reportDuplicateLetDeclarationNames(module)

        val resolvedImports =
                resolveImports(env, repository, sourceFile, module.imports)

        errors.addAll(resolvedImports.errors)

        env = module.declarations.fold(resolvedImports.environment) { e: Environment, d: Declaration ->
            when (d) {
                is za.co.no9.sle.ast.typelessPattern.LetDeclaration -> {
                    val name =
                            d.name.name

                    val scheme =
                            typeToSchemeNullable(d.ttype).first

                    if (scheme == null) {
                        e
                    } else {
                        e.newValue(name, VariableBinding(scheme))
                    }
                }

                is za.co.no9.sle.ast.typelessPattern.TypeAliasDeclaration -> {
                    if (e.containsType(d.name.name)) {
                        errors.add(DuplicateTypeAliasDeclaration(d.location, d.name.name))
                        e
                    } else {
                        e.newType(d.name.name, AliasBinding(typeToScheme(d.ttype).first))
                    }
                }

                is za.co.no9.sle.ast.typelessPattern.TypeDeclaration -> {
                    val newEnv =
                            d.constructors.fold(e) { fds, constructor ->
                                if (fds.containsValue(constructor.name.name)) {
                                    errors.add(DuplicateConstructorDeclaration(constructor.location, constructor.name.name))
                                    fds
                                } else {
                                    val scheme =
                                            d.scheme()

                                    fds.newValue(constructor.name.name, VariableBinding(Scheme(scheme.first.parameters, constructor.arguments.foldRight(scheme.first.type) { a, b -> TArr(transform(a, scheme.second), b) })))
                                }
                            }

                    if (newEnv.containsType(d.name.name)) {
                        errors.add(DuplicateTypeDeclaration(d.location, d.name.name))
                        newEnv
                    } else {
                        val scheme =
                                d.scheme()

                        newEnv.newType(d.name.name, ADTBinding(scheme.first, d.constructors.map {
                            Pair(it.name.name, newEnv.variable(it.name.name)!!)
                        }))
                    }
                }
            }
        }

        for (declaration in module.declarations) {
            when (declaration) {
                is za.co.no9.sle.ast.typelessPattern.LetDeclaration -> {
                    val scheme =
                            typeToSchemeNullable(declaration.ttype).first

                    if (scheme != null) {
                        validateScheme(scheme)
                    }
                }

                is za.co.no9.sle.ast.typelessPattern.TypeAliasDeclaration -> {
                    validateScheme(typeToScheme(declaration.ttype).first)
                }

                is za.co.no9.sle.ast.typelessPattern.TypeDeclaration -> {
                    val scheme =
                            declaration.scheme()

                    validateScheme(scheme.first)
                    for (constructor in declaration.constructors) {
                        for (type in constructor.arguments) {
                            validateType(transform(type, scheme.second))
                        }
                    }
                }
            }
        }


        val declarations =
                module.declarations.fold(emptyList<za.co.no9.sle.ast.typedPattern.Declaration>()) { ds, d ->
                    when (d) {
                        is za.co.no9.sle.ast.typelessPattern.LetDeclaration -> {
                            val es =
                                    d.expressions.map { infer(it) }

                            val dScheme =
                                    typeToSchemeNullable(d.ttype).first

                            if (dScheme == null) {
                                for (e in es.drop(1)) {
                                    unify(es[0].type, e.type)
                                }

                                val unifiesResult =
                                        unifies(varPump, constraints, env)

                                val unifyErrors =
                                        unifiesResult.left()

                                val substitution =
                                        unifiesResult.right()

                                if (unifyErrors != null) {
                                    errors.addAll(unifyErrors)
                                }

                                val scheme =
                                        if (substitution == null)
                                            generalise(es[0].type)
                                        else
                                            generalise(es[0].type, substitution)

                                val declaration =
                                        if (substitution == null)
                                            value(LetDeclaration(d.location, scheme, ID(d.name.location, d.name.name), es))
                                        else
                                            LetDeclaration(d.location, scheme, ID(d.name.location, d.name.name), es).apply(env, substitution)

                                errors.addAll(declaration.left() ?: listOf())

                                env = env.newValue(d.name.name, VariableBinding(scheme))

                                declaration.fold({ ds }, { ds + it })
                            } else {
                                val type =
                                        dScheme.instantiate(varPump)

                                for (e in es) {
                                    unify(type, e.type)
                                }

                                ds + LetDeclaration(d.location, dScheme, ID(d.name.location, d.name.name), es)
                            }
                        }

                        is za.co.no9.sle.ast.typelessPattern.TypeAliasDeclaration ->
                            ds + TypeAliasDeclaration(d.location, ID(d.name.location, d.name.name), typeToScheme(d.ttype).first)

                        is za.co.no9.sle.ast.typelessPattern.TypeDeclaration -> {
                            val scheme =
                                    d.scheme()

                            ds + za.co.no9.sle.ast.typedPattern.TypeDeclaration(
                                    d.location,
                                    ID(d.name.location, d.name.name),
                                    scheme.first,
                                    d.constructors.map { Constructor(it.location, ID(it.name.location, it.name.name), it.arguments.map { transform(it, scheme.second) }) }
                            )
                        }
                    }
                }

        val exports =
                module.exports.map {
                    when (it) {
                        is za.co.no9.sle.ast.typelessPattern.LetExport -> {
                            val scheme =
                                    env.variable(it.name.name)

                            if (scheme == null) {
                                errors.add(UnboundVariable(it.name.location, QString(null, it.name.name)))
                                ValueExportDeclaration(it.name.name, generalise(typeError))
                            } else {
                                ValueExportDeclaration(it.name.name, scheme)
                            }
                        }

                        is za.co.no9.sle.ast.typelessPattern.TypeExport -> {
                            val typeBinding =
                                    env.type(it.name.name)

                            when (typeBinding) {
                                null -> {
                                    errors.add(UnknownTypeReference(it.name.location, QString(it.name.name)))
                                    AliasExportDeclaration(it.name.name, generalise(typeError))
                                }

                                is AliasBinding ->
                                    AliasExportDeclaration(it.name.name, typeBinding.scheme)

                                is ADTBinding ->
                                    if (it.withConstructors)
                                        FullADTExportDeclaration(
                                                it.name.name,
                                                typeBinding.scheme,
                                                typeBinding.constructors.map { pair ->
                                                    ConstructorNameDeclaration(pair.first, pair.second)
                                                })
                                    else
                                        ADTExportDeclaration(it.name.name, typeBinding.scheme)

                                else ->
                                    TODO()
                            }
                        }
                    }
                }

        return Module(
                module.location,
                exports,
                declarations)
    }


    private fun reportDuplicateLetDeclarationNames(module: za.co.no9.sle.ast.typelessPattern.Module) {
        module.declarations.fold(emptySet()) { e: Set<String>, d: Declaration ->
            when (d) {
                is za.co.no9.sle.ast.typelessPattern.LetDeclaration -> {
                    val name =
                            d.name.name

                    if (e.contains(name)) {
                        errors.add(DuplicateLetDeclaration(d.location, name))
                        e
                    } else {
                        e + name
                    }
                }

                is za.co.no9.sle.ast.typelessPattern.TypeAliasDeclaration ->
                    e

                is TypeDeclaration ->
                    e
            }
        }
    }


    private fun validateScheme(scheme: Scheme) {
        validateType(scheme.type)
    }


    private fun validateType(type: Type) {
        when (type) {
            is TCon -> {
                val scheme =
                        env.type(type.name)?.scheme

                if (scheme == null) {
                    errors.add(UnknownTypeReference(type.location, type.name))
                } else if (type.arguments.size != scheme.parameters.size) {
                    errors.add(IncorrectNumberOfSchemeArguments(type.location, type.name, scheme.parameters.size, type.arguments.size))
                }
            }

            is TArr -> {
                validateType(type.domain)
                validateType(type.range)
            }

            else -> {
            }
        }
    }


    private fun infer(expression: za.co.no9.sle.ast.typelessPattern.Expression): Expression =
            when (expression) {
                is za.co.no9.sle.ast.typelessPattern.Unit ->
                    Unit(expression.location, typeUnit)

                is za.co.no9.sle.ast.typelessPattern.ConstantBool ->
                    ConstantBool(expression.location, typeBool, expression.value)

                is za.co.no9.sle.ast.typelessPattern.ConstantInt ->
                    ConstantInt(expression.location, typeInt, expression.value)

                is za.co.no9.sle.ast.typelessPattern.ConstantString ->
                    ConstantString(expression.location, typeString, expression.value)

                is za.co.no9.sle.ast.typelessPattern.IdReference -> {
                    val valueBinding =
                            env.value(expression.name.asQString())

                    val scheme =
                            when (valueBinding) {
                                is VariableBinding ->
                                    valueBinding.scheme

                                is ImportVariableBinding ->
                                    valueBinding.scheme

                                else ->
                                    null
                            }

                    if (scheme == null) {
                        errors.add(UnboundVariable(expression.location, expression.name.asQString()))

                        IdReference(expression.location, typeError, transform(expression.name))
                    } else {
                        val type =
                                scheme.instantiate(varPump)

                        IdReference(expression.location, type, transform(expression.name))
                    }
                }

                is za.co.no9.sle.ast.typelessPattern.ConstructorReference -> {
                    val scheme =
                            env.variable(expression.name.asQString())

                    when (scheme) {
                        null -> {
                            errors.add(UnboundVariable(expression.location, expression.name.asQString()))

                            IdReference(expression.location, typeError, transform(expression.name))
                        }

                        else -> {
                            val type =
                                    scheme.instantiate(varPump)

                            IdReference(expression.location, type, transform(expression.name))
                        }
                    }
                }

                is za.co.no9.sle.ast.typelessPattern.IfExpression -> {
                    val t1 =
                            infer(expression.guardExpression)

                    val t2 =
                            infer(expression.thenExpression)

                    val t3 =
                            infer(expression.elseExpression)

                    unify(t1.type, typeBool)
                    unify(t2.type, t3.type)

                    IfExpression(expression.location, t2.type, t1, t2, t3)
                }

                is za.co.no9.sle.ast.typelessPattern.LambdaExpression -> {
                    val currentEnv = env

                    val tp =
                            infer(expression.argument)

                    val t =
                            infer(expression.expression)

                    env = currentEnv

                    LambdaExpression(expression.location, TArr(tp.type, t.type), tp, t)
                }

                is za.co.no9.sle.ast.typelessPattern.CallExpression -> {
                    val t1 =
                            infer(expression.operator)

                    val t2 =
                            infer(expression.operand)

                    val tv =
                            varPump.fresh(expression.location)

                    unify(t1.type, TArr(t2.type, tv))

                    CallExpression(expression.location, tv, t1, t2)
                }

                is za.co.no9.sle.ast.typelessPattern.CaseExpression -> {
                    val tp =
                            infer(expression.operator)

                    val tr =
                            varPump.fresh(expression.location)

                    CaseExpression(expression.location, tr, tp, expression.items.map {
                        val itemPattern =
                                infer(it.pattern)

                        val itemExpression =
                                infer(it.expression)

                        val currentEnv =
                                env

                        unify(tp.type, itemPattern.type)
                        unify(tr, itemExpression.type)

                        env = currentEnv

                        CaseItem(it.location, itemPattern, itemExpression)
                    })
                }
            }


    private fun infer(pattern: za.co.no9.sle.ast.typelessPattern.Pattern): Pattern =
            when (pattern) {
                is za.co.no9.sle.ast.typelessPattern.ConstantIntPattern ->
                    ConstantIntPattern(pattern.location, typeInt, pattern.value)

                is za.co.no9.sle.ast.typelessPattern.ConstantBoolPattern ->
                    ConstantBoolPattern(pattern.location, typeBool, pattern.value)

                is za.co.no9.sle.ast.typelessPattern.ConstantStringPattern ->
                    ConstantStringPattern(pattern.location, typeString, pattern.value)

                is za.co.no9.sle.ast.typelessPattern.ConstantUnitPattern ->
                    ConstantUnitPattern(pattern.location, typeUnit)

                is za.co.no9.sle.ast.typelessPattern.IdReferencePattern -> {
                    val idType =
                            varPump.fresh(pattern.location)

                    env = env.newValue(pattern.name, VariableBinding(Scheme(emptyList(), idType)))

                    IdReferencePattern(pattern.location, idType, pattern.name)
                }

                is za.co.no9.sle.ast.typelessPattern.ConstructorReferencePattern -> {
                    val constructor =
                            env.variable(pattern.name.asQString())

                    if (constructor == null) {
                        errors.add(UnknownConstructorReference(pattern.location, pattern.name.asQString()))
                        ConstantUnitPattern(pattern.location, typeUnit)
                    } else {
                        if (constructor.type.arity() != pattern.parameters.size) {
                            errors.add(IncorrectNumberOfConstructorArguments(pattern.location, pattern.name.asQString(), constructor.type.arity(), pattern.parameters.size))
                        }

                        val parameters =
                                pattern.parameters.map { infer(it) }

                        val constructorType =
                                constructor.instantiate(varPump)

                        val returnType =
                                constructorType.last()

                        if (parameters.isNotEmpty()) {
                            val signature =
                                    parameters.foldRight(returnType) { a, b -> TArr(a.type, b) }

                            unify(constructorType, signature)
                        }

                        ConstructorReferencePattern(pattern.location, returnType, transform(pattern.name), parameters)
                    }
                }
            }


    private fun unify(t1: Type, t2: Type) {
        constraints += Constraint(t1, t2)
    }
}

private fun za.co.no9.sle.ast.typelessPattern.QualifiedID.asQString(): QString =
        QString(this.qualifier, this.name)


private class ResolveImportsResult(val environment: Environment, val errors: Errors)


private fun resolveImports(environment: Environment, repository: Repository<Item>, sourceFile: File, imports: List<za.co.no9.sle.ast.typelessPattern.Import>): ResolveImportsResult {
    val errors =
            mutableSetOf<Error>()

    val newEnvironment =
            imports.fold(environment) { currentEnvironment, import ->
                val importFile =
                        File(sourceFile.parentFile, import.urn.name)

                val importName =
                        if (import.asName == null)
                            ID(import.location, import.urn.impliedName())
                        else
                            ID(import.asName.location, import.asName.name)

                val importQualifier =
                        if (import.importDeclarations.isEmpty() || import.asName != null)
                            importName.name
                        else
                            null

                val importItem =
                        repository.item(import.urn.source, importFile, importQualifier)

                val exports =
                        importItem.exports()

                if (import.importDeclarations.isEmpty()) {
                    val importEnvironment =
                            exports.declarations.fold(Environment()) { e, d ->
                                when (d) {
                                    is za.co.no9.sle.repository.LetDeclaration ->
                                        e.newValue(d.name, ImportVariableBinding(d.scheme.asScheme(import.location)))

                                    is za.co.no9.sle.repository.AliasDeclaration ->
                                        e.newType(d.alias, ImportAliasBinding(d.scheme.asScheme(import.location)))

                                    is za.co.no9.sle.repository.ADTDeclaration ->
                                        e.newType(d.adt, OpaqueImportADTBinding(d.scheme.asScheme(import.location)))

                                    is za.co.no9.sle.repository.FullADTDeclaration -> {
                                        val envWithADTDeclaration =
                                                e.newType(d.adt, ImportADTBinding(d.scheme.asScheme(import.location), d.constructors.map { Pair(it.name, it.scheme.asScheme(import.location)) }))

                                        d.constructors.fold(envWithADTDeclaration) { a, b ->
                                            a.newValue(b.name, VariableBinding(b.scheme.asScheme(import.location)))
                                        }
                                    }
                                }
                            }

                    currentEnvironment.newValue(importName.name, ImportBinding(importEnvironment))
                } else
                    import.importDeclarations.fold(currentEnvironment) { e, d ->
                        when (d) {
                            is za.co.no9.sle.ast.typelessPattern.ValueImportDeclaration -> {
                                val importDeclaration =
                                        exports[d.name.name]

                                when (importDeclaration) {
                                    null -> {
                                        errors.add(ValueNotExported(d.name.location, d.name.name))
                                        e.newValue(d.name.name, ImportVariableBinding(errorScheme))
                                    }

                                    is za.co.no9.sle.repository.LetDeclaration ->
                                        e.newValue(d.name.name, ImportVariableBinding(importDeclaration.scheme.asScheme(d.name.location)))

                                    else -> {
                                        errors.add(ValueNotExported(d.name.location, d.name.name))
                                        e.newValue(d.name.name, ImportVariableBinding(errorScheme))
                                    }
                                }
                            }

                            is za.co.no9.sle.ast.typelessPattern.TypeImportDeclaration -> {
                                val importDeclaration =
                                        exports[d.name.name]

                                when (importDeclaration) {
                                    null -> {
                                        errors.add(TypeNotExported(d.name.location, d.name.name))
                                        e.newType(d.name.name, ImportAliasBinding(errorScheme))
                                    }

                                    is za.co.no9.sle.repository.LetDeclaration -> {
                                        errors.add(TypeNotExported(d.name.location, d.name.name))
                                        e.newValue(d.name.name, ImportVariableBinding(importDeclaration.scheme.asScheme(d.name.location)))
                                    }

                                    is za.co.no9.sle.repository.AliasDeclaration ->
                                        if (d.withConstructors) {
                                            errors.add(TypeAliasHasNoConstructors(d.name.location, d.name.name))
                                            e.newType(d.name.name, ImportAliasBinding(importDeclaration.scheme.asScheme(d.name.location)))
                                        } else
                                            e.newType(d.name.name, ImportAliasBinding(importDeclaration.scheme.asScheme(d.name.location)))

                                    is za.co.no9.sle.repository.ADTDeclaration ->
                                        if (d.withConstructors) {
                                            errors.add(ADTHasNoConstructors(d.name.location, d.name.name))
                                            e.newType(d.name.name, OpaqueImportADTBinding(importDeclaration.scheme.asScheme(d.name.location)))
                                        } else
                                            e.newType(d.name.name, OpaqueImportADTBinding(importDeclaration.scheme.asScheme(d.name.location)))

                                    is za.co.no9.sle.repository.FullADTDeclaration ->
                                        if (d.withConstructors) {
                                            val envWithADTDeclaration =
                                                    e.newType(d.name.name, ImportADTBinding(importDeclaration.scheme.asScheme(d.name.location), importDeclaration.constructors.map { Pair(it.name, it.scheme.asScheme(d.name.location)) }))

                                            importDeclaration.constructors.fold(envWithADTDeclaration) { a, b ->
                                                a.newValue(b.name, VariableBinding(b.scheme.asScheme(d.name.location)))
                                            }
                                        } else
                                            e.newType(d.name.name, OpaqueImportADTBinding(importDeclaration.scheme.asScheme(d.name.location)))
                                }
                            }
                        }
                    }
            }


    return ResolveImportsResult(newEnvironment, errors)
}


private fun transform(qualifiedID: za.co.no9.sle.ast.typelessPattern.QualifiedID): QualifiedID =
        QualifiedID(qualifiedID.location, qualifiedID.qualifier, qualifiedID.name)


private fun Type.arity(): Int =
        when (this) {
            is TArr ->
                1 + this.range.arity()

            else ->
                0
        }


private fun Type.last(): Type =
        when (this) {
            is TArr ->
                this.range.last()

            else ->
                this
        }


private fun transform(type: TType, substitution: Map<String, TVar> = emptyMap()): Type =
        when (type) {
            is TUnit ->
                typeUnit

            is TVarReference ->
                substitution[type.name] ?: TCon(type.location, QString(type.name))

            is TConstReference ->
                TCon(type.location, QString(type.name.qualifier, type.name.name), type.arguments.map { transform(it, substitution) })

            is TArrow ->
                TArr(transform(type.domain, substitution), transform(type.range, substitution))
        }


private fun typeToScheme(ttype: TType): Pair<Scheme, Map<String, TVar>> {
    val pump =
            VarPump()

    val substitution =
            mutableMapOf<String, TVar>()


    fun map(ttype: TType): Type =
            when (ttype) {
                is TUnit ->
                    typeUnit

                is TVarReference -> {
                    val varRef =
                            substitution[ttype.name]

                    if (varRef == null) {
                        val newVarRef =
                                pump.fresh(ttype.location)

                        substitution[ttype.name] = newVarRef

                        newVarRef
                    } else {
                        varRef
                    }
                }

                is TConstReference ->
                    TCon(ttype.location, QString(ttype.name.qualifier, ttype.name.name), ttype.arguments.map { map(it) })

                is TArrow ->
                    TArr(map(ttype.domain), map(ttype.range))
            }


    val type =
            map(ttype)

    return Pair(Scheme(substitution.values.map { it.variable }, type), substitution)
}


private fun typeToSchemeNullable(ttype: TType?): Pair<Scheme?, Map<String, TVar>> =
        when (ttype) {
            null ->
                Pair(null, mapOf())

            else -> typeToScheme(ttype)
        }


private fun za.co.no9.sle.ast.typelessPattern.TypeDeclaration.scheme(): Pair<Scheme, Map<String, TVar>> {
    val substitution =
            this.arguments.foldIndexed(emptyMap<String, TVar>()) { index, subst, id -> subst + Pair(id.name, TVar(id.location, index)) }

    val parameters =
            this.arguments.mapIndexed { index, _ -> index }

    return Pair(Scheme(parameters, TCon(this.name.location, QString(this.name.name), this.arguments.map { argument -> substitution[argument.name]!! })), substitution)
}