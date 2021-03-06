package za.co.no9.sle.transform.typelessPatternToTypedPattern

import za.co.no9.sle.*
import za.co.no9.sle.ast.typedPattern.*
import za.co.no9.sle.ast.typedPattern.CallExpression
import za.co.no9.sle.ast.typedPattern.CaseExpression
import za.co.no9.sle.ast.typedPattern.CaseItem
import za.co.no9.sle.ast.typedPattern.ConstantBool
import za.co.no9.sle.ast.typedPattern.ConstantBoolPattern
import za.co.no9.sle.ast.typedPattern.ConstantChar
import za.co.no9.sle.ast.typedPattern.ConstantCharPattern
import za.co.no9.sle.ast.typedPattern.ConstantField
import za.co.no9.sle.ast.typedPattern.ConstantInt
import za.co.no9.sle.ast.typedPattern.ConstantIntPattern
import za.co.no9.sle.ast.typedPattern.ConstantRecord
import za.co.no9.sle.ast.typedPattern.ConstantString
import za.co.no9.sle.ast.typedPattern.ConstantStringPattern
import za.co.no9.sle.ast.typedPattern.Constructor
import za.co.no9.sle.ast.typedPattern.ConstructorReferencePattern
import za.co.no9.sle.ast.typedPattern.Declaration
import za.co.no9.sle.ast.typedPattern.Expression
import za.co.no9.sle.ast.typedPattern.FieldProjectionExpression
import za.co.no9.sle.ast.typedPattern.ID
import za.co.no9.sle.ast.typedPattern.IdReference
import za.co.no9.sle.ast.typedPattern.IdReferencePattern
import za.co.no9.sle.ast.typedPattern.IfExpression
import za.co.no9.sle.ast.typedPattern.IgnorePattern
import za.co.no9.sle.ast.typedPattern.LambdaExpression
import za.co.no9.sle.ast.typedPattern.LetDeclaration
import za.co.no9.sle.ast.typedPattern.LetExpression
import za.co.no9.sle.ast.typedPattern.LowerIDDeclarationID
import za.co.no9.sle.ast.typedPattern.Module
import za.co.no9.sle.ast.typedPattern.OperatorDeclarationID
import za.co.no9.sle.ast.typedPattern.Pattern
import za.co.no9.sle.ast.typedPattern.RecordPattern
import za.co.no9.sle.ast.typedPattern.TypeAliasDeclaration
import za.co.no9.sle.ast.typedPattern.TypeDeclaration
import za.co.no9.sle.ast.typedPattern.Unit
import za.co.no9.sle.ast.typedPattern.UpdateRecordExpression
import za.co.no9.sle.ast.typedPattern.ValueDeclarationID
import za.co.no9.sle.ast.typelessPattern.*
import za.co.no9.sle.repository.Item
import za.co.no9.sle.typing.*


data class InferResult(val module: Module, val constraints: Constraints, val environment: Environment)


fun infer(source: Item, varPump: VarPump, module: za.co.no9.sle.ast.typelessPattern.Module, env: Environment): Either<Errors, InferResult> {
    val context =
            InferContext(source, varPump, env)

    val m =
            context.infer(module)

    return if (context.errors.isEmpty())
        value(InferResult(m, context.constraints, context.env))
    else
        error(context.errors)
}


private class InferContext(private val source: Item, private val varPump: VarPump, internal var env: Environment) {
    var constraints =
            Constraints()

    val errors =
            mutableSetOf<Error>()


    fun infer(module: za.co.no9.sle.ast.typelessPattern.Module): Module {
        reportDuplicateLetDeclarationNames(module)

        val resolvedImports =
                resolveImports(env, source, module.imports)

        errors.addAll(resolvedImports.errors)

        env = addDeclarationsIntoEnvironment(resolvedImports.environment, module.declarations)

        errors.addAll(validateDeclarationTTypes(env, module))

        val declarations =
                infer(module.declarations)

        val exports =
                module.exports.map {
                    when (it) {
                        is za.co.no9.sle.ast.typelessPattern.LetExport -> {
                            val valueBinding =
                                    env.value(it.name.name)

                            when (valueBinding) {
                                null, is ImportBinding -> {
                                    errors.add(UnboundVariable(it.name.location, QString(null, it.name.name)))
                                    ValueExportDeclaration(it.name.name, generalise(typeError))
                                }

                                is VariableBinding ->
                                    ValueExportDeclaration(it.name.name, valueBinding.scheme)

                                is OperatorBinding ->
                                    OperatorExportDeclaration(it.name.name, valueBinding.scheme, valueBinding.precedence, valueBinding.associativity)

                                is ImportVariableBinding ->
                                    ValueExportDeclaration(it.name.name, valueBinding.scheme)

                                is ImportOperatorBinding ->
                                    OperatorExportDeclaration(it.name.name, valueBinding.scheme, valueBinding.precedence, valueBinding.associativity)

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

                                is ImportADTBinding -> {
                                    val listOfVars =
                                            listOfVars(typeBinding.cardinality)

                                    val scheme =
                                            Scheme(listOfVars, TCon(it.location, it.name.name, listOfVars.map { v -> TVar(it.location, v) }))

                                    if (it.withConstructors)
                                        FullADTExportDeclaration(
                                                typeBinding.identity,
                                                scheme,
                                                typeBinding.constructors.map { pair ->
                                                    ConstructorNameDeclaration(pair.first, pair.second)
                                                })
                                    else
                                        ADTExportDeclaration(it.name.name, scheme)
                                }

                                else ->
                                    TODO("$typeBinding")
                            }
                        }
                    }
                }

        return Module(
                module.location,
                exports,
                declarations)
    }


    private fun infer(declarations: List<za.co.no9.sle.ast.typelessPattern.Declaration>): List<Declaration> =
            declarations.fold(emptyList()) { ds, d ->
                ds + infer(d)
            }


    private fun infer(d: za.co.no9.sle.ast.typelessPattern.Declaration): Declaration =
            when (d) {
                is za.co.no9.sle.ast.typelessPattern.LetDeclaration -> {
                    val dScheme =
                            env.variable(d.id.name.name)

                    if (dScheme == null) {
                        val es =
                                d.expressions.map { infer(it) }

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
                                    value(LetDeclaration(d.location, scheme, transform(d.id), es))
                                else
                                    LetDeclaration(d.location, scheme, transform(d.id), es).apply(env, substitution)

                        errors.addAll(declaration.left() ?: listOf())

                        env = env.newValue(d.id.name.name, VariableBinding(scheme))

                        declaration.fold({ LetDeclaration(d.location, scheme, transform(d.id), es) }, { it })
                    } else {
                        val type =
                                dScheme.instantiate(varPump)

                        val es =
                                d.expressions.map { infer(it, type) }

                        for (e in es) {
                            unify(type, e.type)
                        }

                        LetDeclaration(d.location, dScheme, transform(d.id), es)
                    }
                }

                is za.co.no9.sle.ast.typelessPattern.TypeAliasDeclaration ->
                    TypeAliasDeclaration(d.location, ID(d.name.location, d.name.name), env.alias(d.name.name)!!)

                is za.co.no9.sle.ast.typelessPattern.TypeDeclaration -> {
                    val scheme =
                            d.scheme(source)

                    TypeDeclaration(
                            d.location,
                            ID(d.name.location, d.name.name),
                            scheme.first,
                            d.constructors.map { Constructor(it.location, ID(it.name.location, it.name.name), it.arguments.map { transform(env, source, it, scheme.second) }) }
                    )
                }
            }


    private fun listOfVars(cardinality: Int): List<Var> {
        val result =
                mutableListOf<Var>()

        for (lp in 0 until cardinality) {
            result.add(lp)
        }

        return result
    }


    private fun addDeclarationsIntoEnvironment(e: Environment, declarations: List<za.co.no9.sle.ast.typelessPattern.Declaration>): Environment =
            declarations.fold(e) { environment, declaration -> addDeclarationIntoEnvironment(environment, declaration) }


    private fun addDeclarationIntoEnvironment(e: Environment, d: za.co.no9.sle.ast.typelessPattern.Declaration): Environment =
            when (d) {
                is za.co.no9.sle.ast.typelessPattern.LetDeclaration -> {
                    val name =
                            d.id.name.name

                    val scheme =
                            typeToSchemeNullable(e, varPump, source, d.ttype)

                    if (scheme == null) {
                        e.removeValue(name)
                    } else {
                        when (d.id) {
                            is za.co.no9.sle.ast.typelessPattern.LowerIDDeclarationID ->
                                e.newValue(name, VariableBinding(scheme))

                            is za.co.no9.sle.ast.typelessPattern.OperatorDeclarationID ->
                                e.newValue(name, OperatorBinding(scheme, d.id.precedence, d.id.associativity))
                        }
                    }
                }

                is za.co.no9.sle.ast.typelessPattern.TypeAliasDeclaration -> {
                    if (e.containsType(d.name.name)) {
                        errors.add(DuplicateTypeAliasDeclaration(d.location, d.name.name))
                        e
                    } else {
                        e.newType(d.name.name, AliasBinding(typeToScheme(e, varPump, source, d.ttype)))
                    }
                }

                is za.co.no9.sle.ast.typelessPattern.TypeDeclaration -> {
                    val scheme =
                            d.scheme(source)

                    val newEnv =
                            d.constructors.fold(e) { fds, constructor ->
                                if (fds.containsValue(constructor.name.name)) {
                                    errors.add(DuplicateConstructorDeclaration(constructor.location, constructor.name.name))
                                    fds
                                } else {
                                    fds.newValue(constructor.name.name, VariableBinding(Scheme(scheme.first.parameters, constructor.arguments.foldRight(scheme.first.type) { a, b -> TArr(transform(e, source, a, scheme.second), b) })))
                                }
                            }

                    if (newEnv.containsType(d.name.name)) {
                        errors.add(DuplicateTypeDeclaration(d.location, d.name.name))
                        newEnv
                    } else {
                        newEnv.newType(d.name.name, ADTBinding(scheme.first, d.constructors.map {
                            Pair(it.name.name, newEnv.variable(it.name.name)!!)
                        }))
                    }
                }
            }


    private fun reportDuplicateLetDeclarationNames(module: za.co.no9.sle.ast.typelessPattern.Module) {
        module.declarations.fold(emptySet()) { e: Set<String>, d: za.co.no9.sle.ast.typelessPattern.Declaration ->
            when (d) {
                is za.co.no9.sle.ast.typelessPattern.LetDeclaration -> {
                    val name =
                            d.id.name.name

                    if (e.contains(name)) {
                        errors.add(DuplicateLetDeclaration(d.location, name))
                        e
                    } else {
                        e + name
                    }
                }

                is za.co.no9.sle.ast.typelessPattern.TypeAliasDeclaration ->
                    e

                is za.co.no9.sle.ast.typelessPattern.TypeDeclaration ->
                    e
            }
        }
    }


    private fun infer(expression: za.co.no9.sle.ast.typelessPattern.Expression, actualType: Type? = null): Expression =
            when (expression) {
                is za.co.no9.sle.ast.typelessPattern.ConstantBool ->
                    ConstantBool(expression.location, actualUnify(actualType, typeBool), expression.value)

                is za.co.no9.sle.ast.typelessPattern.ConstantInt ->
                    ConstantInt(expression.location, actualUnify(actualType, typeInt), expression.value)

                is za.co.no9.sle.ast.typelessPattern.ConstantString ->
                    ConstantString(expression.location, actualUnify(actualType, typeString), expression.value)

                is za.co.no9.sle.ast.typelessPattern.ConstantChar ->
                    ConstantChar(expression.location, actualUnify(actualType, typeChar), expression.value)

                is za.co.no9.sle.ast.typelessPattern.ConstantRecord -> {
                    val fields =
                            expression.fields.map { ConstantField(it.location, transform(it.name), infer(it.value)) }

                    val inferredType =
                            TRec(expression.location, true, fields.map { Pair(it.name.name, it.value.type) })

                    ConstantRecord(expression.location, actualUnify(actualType, inferredType), fields)
                }

                is za.co.no9.sle.ast.typelessPattern.IdReference -> {
                    val valueBinding =
                            env.value(expression.name.asQString())

                    val scheme =
                            when (valueBinding) {
                                is VariableBinding ->
                                    valueBinding.scheme

                                is OperatorBinding ->
                                    valueBinding.scheme

                                is ImportVariableBinding ->
                                    valueBinding.scheme

                                is ImportOperatorBinding ->
                                    valueBinding.scheme

                                else ->
                                    null
                            }

                    if (scheme == null) {
                        errors.add(UnboundVariable(expression.location, expression.name.asQString()))

                        IdReference(expression.location, typeError, expression.name.name)
                    } else {
                        val type =
                                actualUnify(actualType, scheme.instantiate(varPump))

                        when (valueBinding) {
                            is VariableBinding ->
                                IdReference(expression.location, type, expression.name.name)

                            is OperatorBinding ->
                                IdReference(expression.location, type, expression.name.name)

                            is ImportVariableBinding ->
                                IdReference(expression.location, type, valueBinding.item.resolveId(expression.name.name))

                            is ImportOperatorBinding ->
                                IdReference(expression.location, type, valueBinding.item.resolveConstructor(expression.name.name))

                            else ->
                                TODO("Unknown valueBinding: $valueBinding")
                        }
                    }
                }

                is za.co.no9.sle.ast.typelessPattern.ConstructorReference -> {
                    val valueBinding =
                            env.value(expression.name.asQString())

                    val scheme =
                            when (valueBinding) {
                                is VariableBinding ->
                                    valueBinding.scheme

                                is OperatorBinding ->
                                    valueBinding.scheme

                                is ImportVariableBinding ->
                                    valueBinding.scheme

                                is ImportOperatorBinding ->
                                    valueBinding.scheme

                                else ->
                                    null
                            }

                    if (scheme == null) {
                        errors.add(UnboundVariable(expression.location, expression.name.asQString()))

                        IdReference(expression.location, typeError, expression.name.name)
                    } else {
                        val type =
                                actualUnify(actualType, scheme.instantiate(varPump))

                        when (valueBinding) {
                            is VariableBinding ->
                                IdReference(expression.location, type, expression.name.name)

                            is OperatorBinding ->
                                IdReference(expression.location, type, expression.name.name)

                            is ImportVariableBinding ->
                                IdReference(expression.location, type, valueBinding.item.resolveConstructor(expression.name.name))

                            is ImportOperatorBinding ->
                                IdReference(expression.location, type, valueBinding.item.resolveConstructor(expression.name.name))

                            else ->
                                TODO("Unknown valueBinding: $valueBinding")
                        }
                    }
                }

                is za.co.no9.sle.ast.typelessPattern.LetExpression -> {
                    val currentEnv =
                            env

                    env = addDeclarationsIntoEnvironment(env, expression.declarations)

                    errors.addAll(validateDeclarationTTypes(env, expression.declarations))

                    val declarations =
                            infer(expression.declarations)

                    val resultExpression =
                            infer(expression.expression)

                    val resultType =
                             actualUnify(actualType, resultExpression.type)

                    env = currentEnv

                    LetExpression(expression.location, resultType, declarations as List<LetDeclaration>, resultExpression)
                }

                is za.co.no9.sle.ast.typelessPattern.IfExpression -> {
                    val t1 =
                            infer(expression.guardExpression, typeBool)

                    val t2 =
                            infer(expression.thenExpression, actualType)

                    val t3 =
                            infer(expression.elseExpression, actualType)

                    unify(t1.type, typeBool)
                    unify(t2.type, t3.type)

                    val resultType =
                            actualUnify(actualType, t2.type)

                    IfExpression(expression.location, resultType, t1, t2, t3)
                }

                is za.co.no9.sle.ast.typelessPattern.LambdaExpression -> {
                    val currentEnv = env

                    val actualArrowType =
                            splitActualArrow(actualType) ?: Pair(null, null)

                    val tp =
                            infer(expression.argument, actualArrowType.first)

                    val t =
                            infer(expression.expression, actualArrowType.second)

                    env = currentEnv

                    LambdaExpression(expression.location, actualUnify(actualType, TArr(tp.type, t.type)), tp, t)
                }

                is za.co.no9.sle.ast.typelessPattern.BinaryOpExpression -> {
                    val expressionWithAppliedOperatorRules =
                            transformOperators(env, expression)

                    val operator =
                            za.co.no9.sle.ast.typelessPattern.CallExpression(expressionWithAppliedOperatorRules.operator.location, za.co.no9.sle.ast.typelessPattern.IdReference(expressionWithAppliedOperatorRules.operator.location, QualifiedID(expressionWithAppliedOperatorRules.operator.location, null, expressionWithAppliedOperatorRules.operator.name)), expressionWithAppliedOperatorRules.left)

                    infer(za.co.no9.sle.ast.typelessPattern.CallExpression(expressionWithAppliedOperatorRules.location, operator, expressionWithAppliedOperatorRules.right))
                }

                is za.co.no9.sle.ast.typelessPattern.NestedExpressions ->
                    when {
                        expression.expressions.isEmpty() ->
                            Unit(expression.location, actualUnify(actualType, typeUnit))

                        expression.expressions.size == 1 ->
                            infer(expression.expressions[0], actualType)

                        expression.expressions.size > 10 -> {
                            errors.add(TooManyTupleArguments(expression.location, 10, expression.expressions.size))

                            infer(expression.expressions[0])
                        }

                        else -> {
                            val actualTypes =
                                    splitActualTuple(actualType)

                            val callType =
                                    if (actualType != null && actualTypes != null)
                                        actualTypes.foldRight(actualType) { a, b ->
                                            TArr(a, b)
                                        }
                                    else
                                        null

                            val constructorName =
                                    tupleConstructorName(expression.expressions.size)

                            val initial: za.co.no9.sle.ast.typelessPattern.Expression =
                                    za.co.no9.sle.ast.typelessPattern.ConstructorReference(expression.location, za.co.no9.sle.ast.typelessPattern.QualifiedID(expression.location, null, constructorName))

                            infer(expression.expressions.fold(initial) { a, b ->
                                za.co.no9.sle.ast.typelessPattern.CallExpression(a.location + b.location, a, b)
                            }, callType)
                        }
                    }

                is za.co.no9.sle.ast.typelessPattern.CallExpression -> {
                    if (actualType == null) {
                        val t1 =
                                infer(expression.operator)

                        val t2 =
                                infer(expression.operand)

                        val tv =
                                varPump.fresh(expression.location)

                        unify(t1.type, TArr(t2.type, tv))

                        CallExpression(expression.location, tv, t1, t2)
                    } else {
                        val tv =
                                varPump.fresh(expression.location)

                        val t1 =
                                infer(expression.operator, TArr(tv, actualType))

                        val t2 =
                                infer(expression.operand, tv)

                        CallExpression(expression.location, actualType, t1, t2)
                    }
                }

                is za.co.no9.sle.ast.typelessPattern.FieldProjectionExpression -> {
                    val tr =
                            varPump.fresh(expression.location)

                    val te =
                            infer(expression.record)

                    unify(te.type, TRec(false, listOf(Pair(expression.name.name, tr))))

                    FieldProjectionExpression(expression.location, actualUnify(actualType, tr), te, transform(expression.name))
                }

                is za.co.no9.sle.ast.typelessPattern.UpdateRecordExpression -> {
                    val te =
                            infer(expression.record)

                    val resolvedActualType =
                            resolveAlias(env, actualType)

                    val actualNames =
                            when (resolvedActualType) {
                                is TRec ->
                                    emptyMap<String, Type>() + resolvedActualType.fields

                                else ->
                                    emptyMap()
                            }

                    val updates =
                            expression.updates.map { update ->
                                val tv =
                                        infer(update.second, actualNames[update.first.name])

                                unify(te.type, TRec(false, listOf(Pair(update.first.name, tv.type))))

                                Pair(transform(update.first), tv)
                            }

                    UpdateRecordExpression(expression.location, actualUnify(actualType, te.type), te, updates)
                }

                is za.co.no9.sle.ast.typelessPattern.CaseExpression -> {
                    val tp =
                            infer(expression.operator)

                    val tr =
                            varPump.fresh(expression.location)

                    CaseExpression(expression.location, actualUnify(actualType, tr), tp, expression.items.map {
                        val itemPattern =
                                infer(it.pattern)

                        val itemExpression =
                                infer(it.expression, actualType)

                        val currentEnv =
                                env

                        unify(tp.type, itemPattern.type)
                        unify(tr, itemExpression.type)

                        env = currentEnv

                        CaseItem(it.location, itemPattern, itemExpression)
                    })
                }
            }


    private fun splitActualArrow(type: Type?): Pair<Type, Type>? =
            when (type) {
                null ->
                    null

                is TArr ->
                    Pair(type.domain, type.range)

                is TAlias ->
                    splitActualArrow(resolveAlias(env, type))

                else ->
                    null
            }


    private fun splitActualTuple(type: Type?): List<Type>? =
            when (type) {
                null ->
                    null

                is TCon ->
                    if (type.name.startsWith("Tuple"))
                        type.arguments
                    else
                        null

                else ->
                    null
            }


    private fun actualRecordFieldType(type: Type?, name: String): Type? =
            when (type) {
                null ->
                    null

                is TRec ->
                    type.fields.find { it.first == name }?.second

                else ->
                    null
            }


    private fun infer(pattern: za.co.no9.sle.ast.typelessPattern.Pattern, actualType: Type? = null): Pattern =
            when (pattern) {
                is za.co.no9.sle.ast.typelessPattern.ConstantIntPattern ->
                    ConstantIntPattern(pattern.location, actualUnify(actualType, typeInt), pattern.value)

                is za.co.no9.sle.ast.typelessPattern.ConstantBoolPattern ->
                    ConstantBoolPattern(pattern.location, actualUnify(actualType, typeBool), pattern.value)

                is za.co.no9.sle.ast.typelessPattern.ConstantStringPattern ->
                    ConstantStringPattern(pattern.location, actualUnify(actualType, typeString), pattern.value)

                is za.co.no9.sle.ast.typelessPattern.ConstantCharPattern ->
                    ConstantCharPattern(pattern.location, actualUnify(actualType, typeChar), pattern.value)

                is za.co.no9.sle.ast.typelessPattern.ConstantNTuplePattern ->
                    when {
                        pattern.patterns.isEmpty() ->
                            ConstantUnitPattern(pattern.location, actualUnify(actualType, typeUnit))

                        pattern.patterns.size == 1 ->
                            infer(pattern.patterns[0], actualType)

                        pattern.patterns.size > 10 -> {
                            errors.add(TooManyTupleArguments(pattern.location, 10, pattern.patterns.size))

                            infer(pattern.patterns[0])
                        }

                        else -> {
                            val constructorName =
                                    tupleConstructorName(pattern.patterns.size)

                            infer(za.co.no9.sle.ast.typelessPattern.ConstructorReferencePattern(pattern.location, za.co.no9.sle.ast.typelessPattern.QualifiedID(pattern.location, null, constructorName), pattern.patterns), actualType)
                        }
                    }

                is za.co.no9.sle.ast.typelessPattern.IdReferencePattern -> {
                    val idType =
                            varPump.fresh(pattern.location)

                    env = env.newValue(pattern.name, VariableBinding(Scheme(emptyList(), actualUnify(actualType, idType))))

                    IdReferencePattern(pattern.location, idType, pattern.name)
                }

                is za.co.no9.sle.ast.typelessPattern.IgnorePattern -> {
                    val idType =
                            varPump.fresh(pattern.location)

                    IgnorePattern(pattern.location, idType)
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

                        val constructorType =
                                constructor.instantiate(varPump)

                        val parameters =
                                pattern.parameters.map { infer(it) }

                        val returnType =
                                actualUnify(actualType, constructorType.last())

                        if (parameters.isNotEmpty()) {
                            val signature =
                                    parameters.foldRight(returnType) { a, b -> TArr(a.type, b) }

                            unify(constructorType, signature)
                        }

                        val valueBinding =
                                env.value(pattern.name.asQString())

                        val constructorName = when (valueBinding) {
                            is VariableBinding ->
                                pattern.name.name

                            is ImportVariableBinding ->
                                valueBinding.item.resolveConstructor(pattern.name.name)

                            else ->
                                TODO("Unknown valueBinding: $valueBinding")
                        }

                        ConstructorReferencePattern(pattern.location, returnType, constructorName, parameters)
                    }
                }

                is za.co.no9.sle.ast.typelessPattern.RecordPattern -> {
                    val fields =
                            pattern.fields.map { Pair(transform(it.first), infer(it.second, actualRecordFieldType(actualType, it.first.name))) }

                    val inferredType =
                            TRec(pattern.location, true, fields.map { Pair(it.first.name, it.second.type) })

                    RecordPattern(pattern.location, actualUnify(actualType, inferredType), fields)
                }
            }


    private fun actualUnify(actualType: Type?, inferredType: Type): Type =
            when {
                actualType == null ->
                    inferredType

                similar(actualType, inferredType) ->
                    inferredType

                else -> {
                    unify(actualType, inferredType)
                    actualType
                }
            }


    private fun unify(t1: Type, t2: Type) {
        if (!similar(t1, t2))
            constraints += Constraint(t1, t2)
    }
}


private fun transform(id: za.co.no9.sle.ast.typelessPattern.ValueDeclarationID): ValueDeclarationID =
        when (id) {
            is za.co.no9.sle.ast.typelessPattern.LowerIDDeclarationID ->
                LowerIDDeclarationID(id.location, transform(id.name))

            is za.co.no9.sle.ast.typelessPattern.OperatorDeclarationID ->
                OperatorDeclarationID(id.location, transform(id.name), id.precedence, id.associativity)
        }


private fun transform(name: za.co.no9.sle.ast.typelessPattern.ID): ID =
        ID(name.location, name.name)


private fun za.co.no9.sle.ast.typelessPattern.QualifiedID.asQString(): QString =
        QString(this.qualifier, this.name)


class ResolveImportsResult(val environment: Environment, val errors: Errors)


private fun resolveImports(environment: Environment, source: Item, imports: List<za.co.no9.sle.ast.typelessPattern.Import>): ResolveImportsResult {
    val errors =
            mutableSetOf<Error>()

    val newEnvironment =
            imports.fold(environment) { currentEnvironment, import ->
                val importResult =
                        source.itemRelativeTo(import.urn.name)

                val importName =
                        if (import.asName == null)
                            ID(import.location, import.urn.impliedName())
                        else
                            ID(import.asName.location, import.asName.name)

                val importErrors =
                        importResult.left()

                if (importErrors == null) {
                    val importItem =
                            importResult.right()!!

                    if (import.importDeclarations.isEmpty()) {
                        val importAllResult =
                                importAll(Environment(), import.location, importItem)

                        errors.addAll(importAllResult.errors)

                        if (currentEnvironment.containsValue(importName.name)) {
                            errors.add(DuplicateImportedName(import.asName?.location
                                    ?: import.location, importName.name))
                        }

                        currentEnvironment.newValue(importName.name, ImportBinding(importAllResult.environment))
                    } else {
                        val exportsResult =
                                importItem.exports()

                        if (exportsResult.left() == null) {
                            val exports =
                                    exportsResult.right()!!

                            import.importDeclarations.fold(currentEnvironment) { e, d ->
                                when (d) {
                                    is za.co.no9.sle.ast.typelessPattern.ValueImportDeclaration -> {
                                        val importDeclaration =
                                                exports[d.name.name]

                                        when (importDeclaration) {
                                            null -> {
                                                errors.add(ValueNotExported(d.name.location, d.name.name))
                                                e.newValue(d.name.name, ImportVariableBinding(importItem, errorScheme))
                                            }

                                            is za.co.no9.sle.repository.LetDeclaration -> {
                                                if (e.containsValue(d.name.name)) {
                                                    errors.add(DuplicateImportedLetDeclaration(d.name.location, d.name.name))
                                                }

                                                e.newValue(d.name.name, ImportVariableBinding(importItem, importDeclaration.scheme.asScheme(d.name.location)))
                                            }

                                            is za.co.no9.sle.repository.OperatorDeclaration -> {
                                                if (e.containsValue(importDeclaration.operator)) {
                                                    errors.add(DuplicateImportedLetDeclaration(import.location, importDeclaration.operator))
                                                }

                                                e.newValue(importDeclaration.operator, OperatorBinding(importDeclaration.scheme.asScheme(import.location), importDeclaration.precedence, associativityFromString(importDeclaration.associativity)))
                                            }

                                            else -> {
                                                errors.add(ValueNotExported(d.name.location, d.name.name))
                                                e.newValue(d.name.name, ImportVariableBinding(importItem, errorScheme))
                                            }
                                        }
                                    }

                                    is za.co.no9.sle.ast.typelessPattern.OperatorImportDeclaration -> {
                                        val importDeclaration =
                                                exports[d.name.name]

                                        when (importDeclaration) {
                                            null -> {
                                                errors.add(ValueNotExported(d.name.location, d.name.name))
                                                e.newValue(d.name.name, ImportVariableBinding(importItem, errorScheme))
                                            }

                                            is za.co.no9.sle.repository.OperatorDeclaration -> {
                                                if (e.containsValue(d.name.name)) {
                                                    errors.add(DuplicateImportedLetDeclaration(d.name.location, d.name.name))
                                                }

                                                e.newValue(d.name.name, ImportOperatorBinding(importItem, importDeclaration.scheme.asScheme(d.name.location), importDeclaration.precedence, associativityFromString(importDeclaration.associativity)))
                                            }

                                            else -> {
                                                errors.add(ValueNotExported(d.name.location, d.name.name))
                                                e.newValue(d.name.name, ImportVariableBinding(importItem, errorScheme))
                                            }
                                        }
                                    }

                                    is za.co.no9.sle.ast.typelessPattern.TypeImportDeclaration -> {
                                        val importDeclaration =
                                                exports[d.name.name]

                                        when (importDeclaration) {
                                            null -> {
                                                errors.add(TypeNotExported(d.name.location, d.name.name))
                                                e.newType(d.name.name, ImportAliasBinding(importItem, errorScheme))
                                            }

                                            is za.co.no9.sle.repository.LetDeclaration -> {
                                                errors.add(TypeNotExported(d.name.location, d.name.name))
                                                e.newValue(d.name.name, ImportVariableBinding(importItem, importDeclaration.scheme.asScheme(d.name.location)))
                                            }

                                            is za.co.no9.sle.repository.OperatorDeclaration -> {
                                                errors.add(TypeNotExported(d.name.location, d.name.name))
                                                e.newValue(d.name.name, ImportVariableBinding(importItem, importDeclaration.scheme.asScheme(d.name.location)))
                                            }

                                            is za.co.no9.sle.repository.AliasDeclaration ->
                                                when {
                                                    d.withConstructors -> {
                                                        errors.add(TypeAliasHasNoConstructors(d.name.location, d.name.name))
                                                        e.newType(d.name.name, ImportAliasBinding(importItem, importDeclaration.scheme.asScheme(d.name.location)))
                                                    }

                                                    e.containsType(d.name.name) -> {
                                                        errors.add(DuplicateImportedTypeAliasDeclaration(d.location, d.name.name))
                                                        e.newType(d.name.name, ImportAliasBinding(importItem, importDeclaration.scheme.asScheme(d.name.location)))
                                                    }

                                                    else ->
                                                        e.newType(d.name.name, ImportAliasBinding(importItem, importDeclaration.scheme.asScheme(d.name.location)))
                                                }

                                            is za.co.no9.sle.repository.OpaqueADTDeclaration -> {
                                                if (e.containsType(d.name.name)) {
                                                    errors.add(DuplicateImportedTypeDeclaration(d.location, d.name.name))
                                                }

                                                if (d.withConstructors) {
                                                    errors.add(ADTHasNoConstructors(d.name.location, d.name.name))
                                                    e.newType(d.name.name, OpaqueImportADTBinding(importItem, importDeclaration.cardinality, importDeclaration.identity))
                                                } else
                                                    e.newType(d.name.name, OpaqueImportADTBinding(importItem, importDeclaration.cardinality, importDeclaration.identity))
                                            }

                                            is za.co.no9.sle.repository.ADTDeclaration -> {
                                                if (e.containsType(d.name.name)) {
                                                    errors.add(DuplicateImportedTypeDeclaration(d.location, d.name.name))
                                                }

                                                if (d.withConstructors) {
                                                    val envWithADTDeclaration =
                                                            e.newType(d.name.name, ImportADTBinding(importItem, importDeclaration.cardinality, importDeclaration.identity, importDeclaration.constructors.map { Pair(it.name, it.scheme.asScheme(d.name.location)) }))

                                                    importDeclaration.constructors.fold(envWithADTDeclaration) { a, b ->
                                                        a.newValue(b.name, ImportVariableBinding(importItem, b.scheme.asScheme(d.name.location)))
                                                    }
                                                } else
                                                    e.newType(d.name.name, OpaqueImportADTBinding(importItem, importDeclaration.cardinality, importDeclaration.identity))
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            errors.addAll(exportsResult.left()!!)
                            currentEnvironment
                        }
                    }
                } else {
                    errors.add(ImportErrors(import.location, import.urn, importErrors))
                    currentEnvironment
                }
            }


    return ResolveImportsResult(newEnvironment, errors)
}


fun importAll(environment: Environment, location: Location, importedItem: Item): ResolveImportsResult {
    val errors =
            mutableSetOf<Error>()

    val exportsResult =
            importedItem.exports()

    val importedEnvironment =
            if (exportsResult.left() == null) {
                val exports =
                        exportsResult.right()!!

                exports.declarations.fold(environment) { e, d ->
                    when (d) {
                        is za.co.no9.sle.repository.LetDeclaration -> {
                            if (e.containsValue(d.name)) {
                                errors.add(DuplicateImportedLetDeclaration(location, d.name))
                            }

                            e.newValue(d.name, ImportVariableBinding(importedItem, d.scheme.asScheme(location)))
                        }

                        is za.co.no9.sle.repository.OperatorDeclaration -> {
                            if (e.containsValue(d.operator)) {
                                errors.add(DuplicateImportedLetDeclaration(location, d.operator))
                            }

                            e.newValue(d.operator, ImportOperatorBinding(importedItem, d.scheme.asScheme(location), d.precedence, associativityFromString(d.associativity)))
                        }

                        is za.co.no9.sle.repository.AliasDeclaration -> {
                            if (e.containsType(d.alias)) {
                                errors.add(DuplicateImportedTypeAliasDeclaration(location, d.alias))
                            }

                            e.newType(d.alias, ImportAliasBinding(importedItem, d.scheme.asScheme(location)))
                        }

                        is za.co.no9.sle.repository.OpaqueADTDeclaration -> {
                            if (e.containsType(d.adt)) {
                                errors.add(DuplicateImportedTypeDeclaration(location, d.adt))
                            }
                            e.newType(d.adt, OpaqueImportADTBinding(importedItem, d.cardinality, d.identity))
                        }

                        is za.co.no9.sle.repository.ADTDeclaration -> {
                            if (e.containsType(d.adt)) {
                                errors.add(DuplicateImportedTypeDeclaration(location, d.adt))
                            }

                            val envWithADTDeclaration =
                                    e.newType(d.adt, ImportADTBinding(importedItem, d.cardinality, d.identity, d.constructors.map { Pair(it.name, it.scheme.asScheme(location)) }))

                            d.constructors.fold(envWithADTDeclaration) { a, b ->
                                a.newValue(b.name, ImportVariableBinding(importedItem, b.scheme.asScheme(location)))
                            }
                        }
                    }
                }
            } else {
                errors.addAll(exportsResult.left()!!)
                environment
            }

    return ResolveImportsResult(importedEnvironment, errors)
}


private fun validateDeclarationTTypes(env: Environment, module: za.co.no9.sle.ast.typelessPattern.Module): Errors =
        ValidateTTypes(env).validate(module)


private fun validateDeclarationTTypes(env: Environment, declarations: List<za.co.no9.sle.ast.typelessPattern.Declaration>): Errors =
        ValidateTTypes(env).validate(declarations)


private class ValidateTTypes(val env: Environment) {
    val errors =
            mutableSetOf<Error>()

    private var numberOfTypeParameters: Map<String, Int> =
            emptyMap()


    fun validate(module: za.co.no9.sle.ast.typelessPattern.Module): Errors {
        numberOfTypeParameters =
                module.declarations.fold(emptyMap()) { m, d ->
                    when (d) {
                        is za.co.no9.sle.ast.typelessPattern.TypeAliasDeclaration ->
                            m + Pair(d.name.name, 0)

                        is za.co.no9.sle.ast.typelessPattern.TypeDeclaration ->
                            m + Pair(d.name.name, d.arguments.size)

                        else ->
                            m
                    }
                }


        validateDeclarations(module.declarations)

        return errors
    }


    fun validate(declarations: List<za.co.no9.sle.ast.typelessPattern.Declaration>): Errors {
        numberOfTypeParameters =
                emptyMap()

        validateDeclarations(declarations)

        return errors
    }


    private fun validateDeclarations(declarations: List<za.co.no9.sle.ast.typelessPattern.Declaration>) {
        declarations.forEach { declaration ->
            validateDeclaration(declaration)
        }
    }


    private fun validateDeclaration(declaration: za.co.no9.sle.ast.typelessPattern.Declaration) {
        when (declaration) {
            is za.co.no9.sle.ast.typelessPattern.LetDeclaration ->
                validateTType(declaration.ttype)

            is za.co.no9.sle.ast.typelessPattern.TypeAliasDeclaration ->
                validateTType(declaration.ttype)

            is za.co.no9.sle.ast.typelessPattern.TypeDeclaration ->
                for (constructor in declaration.constructors) {
                    for (type in constructor.arguments) {
                        validateTType(type)
                    }
                }
        }
    }

    private fun validateTType(ttype: TType?) {
        when (ttype) {
            null -> {
            }

            is TNTuple -> {
                ttype.types.forEach {
                    validateTType(it)
                }
            }

            is TTypeReference -> {
                val qualifiedName =
                        QString(ttype.name.qualifier, ttype.name.name)

                val typeBinding =
                        env.type(qualifiedName)

                if (typeBinding == null) {
                    val declaration =
                            numberOfTypeParameters[qualifiedName.string]

                    if (declaration == null) {
                        errors.add(UnknownTypeReference(ttype.location, qualifiedName))
                    } else if (ttype.arguments.size != declaration) {
                        errors.add(IncorrectNumberOfSchemeArguments(ttype.location, qualifiedName, declaration, ttype.arguments.size))
                    }
                } else if (ttype.arguments.size != typeBinding.numberOfParameters()) {
                    errors.add(IncorrectNumberOfSchemeArguments(ttype.location, qualifiedName, typeBinding.numberOfParameters(), ttype.arguments.size))
                }
            }

            is TArrow -> {
                validateTType(ttype.domain)
                validateTType(ttype.range)
            }

            else -> {
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


private fun transform(env: Environment, source: Item, ttype: TType, substitution: Map<String, TVar> = emptyMap()): Type =
        when (ttype) {
            is TNTuple ->
                when {
                    ttype.types.isEmpty() ->
                        typeUnit

                    ttype.types.size == 1 ->
                        transform(env, source, ttype.types[0], substitution)

                    else -> {
                        val constructorName =
                                tupleConstructorName(ttype.types.size)

                        transform(env, source,
                                TTypeReference(ttype.location, QualifiedID(ttype.location, null, constructorName), ttype.types), substitution)
                    }
                }

            is TVarReference ->
                substitution[ttype.name]!!

            is TTypeReference ->
                if (env.isAlias(ttype.name.asQString()))
                    TAlias(ttype.location, QString(ttype.name.qualifier, ttype.name.name), ttype.arguments.map { transform(env, source, it, substitution) })
                else {
                    val typeBinding =
                            env.type(ttype.name.asQString())

                    when (typeBinding) {
                        is BuiltinBinding ->
                            TCon(ttype.location, (typeBinding.scheme.type as TCon).name, ttype.arguments.map { transform(env, source, it, substitution) })

                        is ADTBinding ->
                            TCon(ttype.location, source.resolveConstructor(ttype.name.name), ttype.arguments.map { transform(env, source, it, substitution) })

                        is OpaqueImportADTBinding ->
                            TCon(ttype.location, typeBinding.identity, ttype.arguments.map { transform(env, source, it, substitution) })

                        is ImportADTBinding ->
                            TCon(ttype.location, typeBinding.identity, ttype.arguments.map { transform(env, source, it, substitution) })

                        else ->
                            TCon(ttype.location, source.resolveConstructor(ttype.name.name), ttype.arguments.map { transform(env, source, it, substitution) })
                    }
                }

            is TArrow ->
                TArr(transform(env, source, ttype.domain, substitution), transform(env, source, ttype.range, substitution))

            is TRecord ->
                TRec(true, ttype.fields.map { Pair(it.first.name, transform(env, source, it.second, substitution)) })
        }


private fun typeToScheme(env: Environment, varPump: VarPump, source: Item, ttype: TType): Scheme {
    val substitution =
            mutableMapOf<String, TVar>()


    fun map(ttype: TType): Type =
            when (ttype) {
                is TNTuple ->
                    when {
                        ttype.types.isEmpty() ->
                            typeUnit

                        ttype.types.size == 1 ->
                            map(ttype.types[0])

                        else -> {
                            val constructorName =
                                    tupleConstructorName(ttype.types.size)

                            map(TTypeReference(ttype.location, QualifiedID(ttype.location, null, constructorName), ttype.types))
                        }
                    }

                is TVarReference -> {
                    val varRef =
                            substitution[ttype.name]

                    if (varRef == null) {
                        val newVarRef =
                                varPump.fresh(ttype.location)

                        substitution[ttype.name] = newVarRef

                        newVarRef
                    } else {
                        varRef
                    }
                }

                is TTypeReference ->
                    if (env.isAlias(ttype.name.asQString()))
                        TAlias(ttype.location, QString(ttype.name.qualifier, ttype.name.name), ttype.arguments.map { map(it) })
                    else {
                        val typeBinding =
                                env.type(ttype.name.asQString())

                        when (typeBinding) {
                            is BuiltinBinding ->
                                TCon(ttype.location, (typeBinding.scheme.type as TCon).name, ttype.arguments.map { map(it) })

                            is ADTBinding ->
                                TCon(ttype.location, source.resolveConstructor(ttype.name.name), ttype.arguments.map { map(it) })

                            is OpaqueImportADTBinding ->
                                TCon(ttype.location, typeBinding.identity, ttype.arguments.map { map(it) })

                            is ImportADTBinding ->
                                TCon(ttype.location, typeBinding.identity, ttype.arguments.map { map(it) })

                            else ->
                                TCon(ttype.location, source.resolveConstructor(ttype.name.name), ttype.arguments.map { map(it) })
                        }
                    }

                is TArrow ->
                    TArr(map(ttype.domain), map(ttype.range))

                is TRecord ->
                    TRec(true, ttype.fields.map { Pair(it.first.name, map(it.second)) })
            }


    val type =
            map(ttype)

    return Scheme(substitution.values.map { it.variable }, type)
}


private fun typeToSchemeNullable(env: Environment, varPump: VarPump, source: Item, ttype: TType?): Scheme? =
        when (ttype) {
            null ->
                null

            else ->
                typeToScheme(env, varPump, source, ttype)
        }


private fun Environment.isAlias(name: QString): Boolean =
        this.alias(name) != null


private fun za.co.no9.sle.ast.typelessPattern.TypeDeclaration.scheme(source: Item): Pair<Scheme, Map<String, TVar>> {
    val substitution =
            this.arguments.foldIndexed(emptyMap<String, TVar>()) { index, subst, id -> subst + Pair(id.name, TVar(id.location, index)) }

    val parameters =
            this.arguments.mapIndexed { index, _ -> index }

    return Pair(Scheme(parameters, TCon(this.name.location, source.resolveConstructor(this.name.name), this.arguments.map { argument -> substitution[argument.name]!! })), substitution)
}


private fun tupleConstructorName(numberOfArguments: Int): String =
        "Tuple$numberOfArguments"