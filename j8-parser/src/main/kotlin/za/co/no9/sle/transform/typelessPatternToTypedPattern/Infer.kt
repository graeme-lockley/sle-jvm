package za.co.no9.sle.transform.typelessPatternToTypedPattern

import za.co.no9.sle.*
import za.co.no9.sle.ast.typedPattern.*
import za.co.no9.sle.ast.typedPattern.Unit
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

        val imports =
                resolveImports(repository, sourceFile, module.imports)

        env = incorporateImportsIntoEnvironment(imports, env)

        env = module.declarations.fold(env) { e: Environment, d: Declaration ->
            when (d) {
                is za.co.no9.sle.ast.typelessPattern.LetDeclaration -> {
                    val name =
                            d.name.name

                    val scheme =
                            d.scheme

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
                        e.newType(d.name.name, AliasBinding(d.scheme))
                    }
                }

                is za.co.no9.sle.ast.typelessPattern.TypeDeclaration -> {
                    val newEnv =
                            d.constructors.fold(e) { fds, constructor ->
                                if (fds.containsValue(constructor.name.name)) {
                                    errors.add(DuplicateConstructorDeclaration(constructor.location, constructor.name.name))
                                    fds
                                } else {
                                    fds.newValue(constructor.name.name, VariableBinding(Scheme(d.scheme.parameters, constructor.arguments.foldRight(d.scheme.type) { a, b -> TArr(a, b) })))
                                }
                            }

                    if (newEnv.containsType(d.name.name)) {
                        errors.add(DuplicateTypeDeclaration(d.location, d.name.name))
                        newEnv
                    } else {
                        newEnv.newType(d.name.name, ADTBinding(d.scheme, d.constructors.map {
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
                            declaration.scheme

                    if (scheme != null) {
                        validateScheme(scheme)
                    }
                }

                is za.co.no9.sle.ast.typelessPattern.TypeAliasDeclaration -> {
                    validateScheme(declaration.scheme)
                }

                is za.co.no9.sle.ast.typelessPattern.TypeDeclaration -> {
                    validateScheme(declaration.scheme)
                    for (constructor in declaration.constructors) {
                        for (type in constructor.arguments) {
                            validateType(type)
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
                                    d.scheme

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
                            ds + TypeAliasDeclaration(d.location, ID(d.name.location, d.name.name), d.scheme)

                        is za.co.no9.sle.ast.typelessPattern.TypeDeclaration ->
                            ds + za.co.no9.sle.ast.typedPattern.TypeDeclaration(
                                    d.location,
                                    ID(d.name.location, d.name.name),
                                    d.scheme,
                                    d.constructors.map { Constructor(it.location, ID(it.name.location, it.name.name), it.arguments) }
                            )
                    }
                }

        val exports =
                module.exports.map {
                    when (it) {
                        is za.co.no9.sle.ast.typelessPattern.LetExport -> {
                            val scheme =
                                    env.variable(it.name.name)

                            if (scheme == null) {
                                errors.add(UnboundVariable(it.name.location, it.name.name))
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
                                    errors.add(UnknownTypeReference(it.name.location, it.name.name))
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
                imports,
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
                        env.typeBindings[type.name]?.scheme

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
                    val scheme =
                            env.value(expression.name)

                    when (scheme) {
                        is VariableBinding -> {
                            val type =
                                    scheme.scheme.instantiate(varPump)

                            IdReference(expression.location, type, expression.name)
                        }

                        is ImportVariableBinding -> {
                            val type =
                                    scheme.scheme.instantiate(varPump)

                            IdReference(expression.location, type, expression.name)
                        }

                        else -> {
                            errors.add(UnboundVariable(expression.location, expression.name))

                            IdReference(expression.location, typeError, expression.name)
                        }
                    }
                }

                is za.co.no9.sle.ast.typelessPattern.ConstructorReference -> {
                    val scheme =
                            env.variable(expression.name)

                    when (scheme) {
                        null -> {
                            errors.add(UnboundVariable(expression.location, expression.name))

                            IdReference(expression.location, typeError, expression.name)
                        }

                        else -> {
                            val type =
                                    scheme.instantiate(varPump)

                            IdReference(expression.location, type, expression.name)
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
                            env.variable(pattern.name)

                    if (constructor == null) {
                        errors.add(UnknownConstructorReference(pattern.location, pattern.name))
                        ConstantUnitPattern(pattern.location, typeUnit)
                    } else {
                        if (constructor.type.arity() != pattern.parameters.size) {
                            errors.add(IncorrectNumberOfConstructorArguments(pattern.location, pattern.name, constructor.type.arity(), pattern.parameters.size))
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

                        ConstructorReferencePattern(pattern.location, returnType, pattern.name, parameters)
                    }
                }
            }


    private fun unify(t1: Type, t2: Type) {
        constraints += Constraint(t1, t2)
    }
}


private fun resolveImports(repository: Repository<Item>, sourceFile: File, imports: List<za.co.no9.sle.ast.typelessPattern.Import>): List<Import> {
    val errors =
            mutableSetOf<Error>()

    return imports.map { import ->
        val importFile =
                File(sourceFile.parentFile, import.urn.name)

        val importItem =
                repository.item(import.urn.source, importFile)

        val exports =
                importItem.exports()

        val importName =
                if (import.asName == null)
                    ID(import.location, import.urn.impliedName())
                else
                    ID(import.asName.location, import.asName.name)

        Import(import.location, importName,
                import.importDeclarations.map { d ->
                    when (d) {
                        is za.co.no9.sle.ast.typelessPattern.ValueImportDeclaration -> {
                            val importDeclaration =
                                    exports[d.name.name]

                            when (importDeclaration) {
                                null -> {
                                    errors.add(ValueNotExported(d.name.location, d.name.name))
                                    ValueImportDeclaration(d.name.location, ID(d.name.location, d.name.name), errorScheme)
                                }

                                is za.co.no9.sle.repository.LetDeclaration ->
                                    ValueImportDeclaration(d.name.location, ID(d.name.location, d.name.name), importDeclaration.scheme.asScheme(d.name.location))

                                else -> {
                                    errors.add(ValueNotExported(d.name.location, d.name.name))
                                    ValueImportDeclaration(d.name.location, ID(d.name.location, d.name.name), errorScheme)
                                }
                            }
                        }

                        is za.co.no9.sle.ast.typelessPattern.TypeImportDeclaration -> {
                            val importDeclaration =
                                    exports[d.name.name]

                            when (importDeclaration) {
                                null -> {
                                    errors.add(TypeNotExported(d.name.location, d.name.name))
                                    AliasImportDeclaration(d.name.location, ID(d.name.location, d.name.name), errorScheme)
                                }

                                is za.co.no9.sle.repository.LetDeclaration -> {
                                    errors.add(TypeNotExported(d.name.location, d.name.name))
                                    ValueImportDeclaration(d.name.location, ID(d.name.location, d.name.name), importDeclaration.scheme.asScheme(d.name.location))
                                }

                                is za.co.no9.sle.repository.AliasDeclaration ->
                                    if (d.withConstructors) {
                                        errors.add(TypeAliasHasNoConstructors(d.name.location, d.name.name))
                                        AliasImportDeclaration(d.name.location, ID(d.name.location, d.name.name), importDeclaration.scheme.asScheme(d.name.location))
                                    } else
                                        AliasImportDeclaration(d.name.location, ID(d.name.location, d.name.name), importDeclaration.scheme.asScheme(d.name.location))

                                is za.co.no9.sle.repository.ADTDeclaration ->
                                    if (d.withConstructors) {
                                        errors.add(ADTHasNoConstructors(d.name.location, d.name.name))
                                        ADTImportDeclaration(d.name.location, ID(d.name.location, d.name.name), importDeclaration.scheme.asScheme(d.name.location))
                                    } else
                                        ADTImportDeclaration(d.name.location, ID(d.name.location, d.name.name), importDeclaration.scheme.asScheme(d.name.location))

                                is za.co.no9.sle.repository.FullADTDeclaration ->
                                    if (d.withConstructors) {
                                        FullADTImportDeclaration(d.name.location, ID(d.name.location, d.name.name), importDeclaration.scheme.asScheme(d.name.location), importDeclaration.constructors.map { ConstructorImportDeclaration(it.name, it.scheme.asScheme(d.name.location)) })
                                    } else
                                        ADTImportDeclaration(d.name.location, ID(d.name.location, d.name.name), importDeclaration.scheme.asScheme(d.name.location))
                            }
                        }
                    }
                })
    }
}


private fun incorporateImportsIntoEnvironment(imports: List<Import>, environment: Environment): Environment =
        imports.fold(environment) { env, import ->
            import.namedDeclarations.fold(env) { e, namedDeclaration ->
                when (namedDeclaration) {
                    is ValueImportDeclaration -> {
                        val valueInEnvironment =
                                e.value(namedDeclaration.name.name)

                        if (valueInEnvironment == null) {
                            e.newValue(namedDeclaration.name.name, ImportVariableBinding(namedDeclaration.scheme))
                        } else {
                            e
                        }
                    }

                    is AliasImportDeclaration ->
                        TODO()

                    is ADTImportDeclaration ->
                        TODO()

                    is FullADTImportDeclaration -> {
                        val envWithADTDeclaration =
                                e.newType(namedDeclaration.name.name, ImportADTBinding(namedDeclaration.scheme, namedDeclaration.constructors.map { Pair(it.name, it.scheme) }))

                        val envWithConstructors =
                                namedDeclaration.constructors.fold(envWithADTDeclaration) { fds, constructor ->
                                    if (fds.containsValue(constructor.name)) {
//                                        errors.add(DuplicateConstructorDeclaration(constructor.location, constructor.name.name))
                                        fds
                                    } else {
                                        fds.newValue(constructor.name, VariableBinding(Scheme(namedDeclaration.scheme.parameters, constructor.scheme.type)))
                                    }
                                }


                        envWithConstructors
                    }
                }
            }
        }


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


