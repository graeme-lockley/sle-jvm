package za.co.no9.sle.transform.typelessPatternToTypedPattern

import za.co.no9.sle.*
import za.co.no9.sle.ast.typedPattern.*
import za.co.no9.sle.ast.typedPattern.Unit
import za.co.no9.sle.typing.*


private typealias Unifier =
        Pair<Substitution, Constraints>


fun unifies(varPump: VarPump, constraints: Constraints, environment: Environment): Either<Errors, Substitution> {
    val context =
            SolverContext(varPump, constraints.leftVar().merge(), environment)

    val subst =
            context.solve()

    return if (context.errors.isEmpty())
        value(subst)
    else
        error(context.errors)
}


fun Module.apply(environment: Environment, substitution: Substitution): Either<Errors, Module> {
    val applyContext =
            ApplyContext(environment)

    val module =
            applyContext.apply(this, substitution)

    return if (applyContext.errors.isEmpty())
        value(module)
    else
        error(applyContext.errors)
}


fun LetDeclaration.apply(environment: Environment, substitution: Substitution): Either<Errors, LetDeclaration> {
    val applyContext =
            ApplyContext(environment)

    val module =
            applyContext.apply(this, substitution)

    return if (applyContext.errors.isEmpty())
        value(module)
    else
        error(applyContext.errors)
}


private class ApplyContext(private val environment: Environment) {
    val errors =
            mutableSetOf<Error>()


    fun apply(module: Module, substitution: Substitution): Module =
            Module(
                    module.location,
                    module.exports.map { apply(it, substitution) },
                    module.declarations.map {
                        when (it) {
                            is LetDeclaration ->
                                apply(it, substitution)

                            is TypeAliasDeclaration ->
                                it

                            is TypeDeclaration ->
                                it
                        }
                    })


    fun apply(exportDeclaration: ExportDeclaration, substitution: Substitution): ExportDeclaration =
            when (exportDeclaration) {
                is ValueExportDeclaration ->
                    ValueExportDeclaration(exportDeclaration.name, exportDeclaration.scheme.apply(substitution).expandAliases(environment).normalize())

                is OperatorExportDeclaration ->
                    OperatorExportDeclaration(exportDeclaration.name, exportDeclaration.scheme.apply(substitution).expandAliases(environment).normalize(), exportDeclaration.precedence, exportDeclaration.associativity)

                is AliasExportDeclaration ->
                    AliasExportDeclaration(exportDeclaration.name, exportDeclaration.scheme.expandAliases(environment).normalize())

                is ADTExportDeclaration ->
                    ADTExportDeclaration(exportDeclaration.name, exportDeclaration.scheme.expandAliases(environment).normalize())

                is FullADTExportDeclaration ->
                    FullADTExportDeclaration(
                            exportDeclaration.name,
                            exportDeclaration.scheme.expandAliases(environment).normalize(),
                            exportDeclaration.constructors.map {
                                ConstructorNameDeclaration(it.name, it.scheme.expandAliases(environment).normalize())
                            })
            }


    fun apply(letDeclaration: LetDeclaration, substitution: Substitution): LetDeclaration {
        val appliedExpressions =
                letDeclaration.expressions.map { apply(it, substitution) }

        val schemes =
                appliedExpressions.map { generalise(it.type).normalize() }

        for (scheme in schemes) {
            if (!scheme.isCompatibleWith(environment, letDeclaration.scheme)) {
                errors.add(IncompatibleDeclarationSignature(letDeclaration.location, letDeclaration.id.name.name, letDeclaration.scheme, scheme))
            }
        }

        return LetDeclaration(letDeclaration.location, letDeclaration.scheme, letDeclaration.id, appliedExpressions)
    }


    private fun Scheme.expandAliases(environment: Environment): Scheme =
            Scheme(parameters, type.expandAliases(environment))


    private fun Type.expandAliases(environment: Environment): Type =
            when (this) {
                is TVar ->
                    this

                is TAlias -> {
                    val alias =
                            environment.alias(name)

                    when {
                        alias == null ->
                            this

                        arguments.size != alias.parameters.size -> {
                            errors.add(IncorrectNumberOfAliasArguments(this.location, name, alias.parameters.size, arguments.size))
                            this
                        }

                        else -> {
                            val substitutionMap =
                                    alias.parameters.zip(arguments).fold(emptyMap<Var, Type>()) { a, b -> a + b }

                            alias.type.apply(Substitution(substitutionMap)).expandAliases(environment)
                        }
                    }
                }

                is TCon ->
                    this

                is TArr ->
                    TArr(domain.expandAliases(environment), range.expandAliases(environment))

                is TRec ->
                    TRec(location, fixed, fields.map { Pair(it.first, it.second.expandAliases(environment)) })
            }


    private fun apply(expression: Expression, substitution: Substitution): Expression =
            when (expression) {
                is Unit ->
                    expression

                is ConstantBool ->
                    expression

                is ConstantInt ->
                    expression

                is ConstantString ->
                    expression

                is ConstantChar ->
                    expression

                is za.co.no9.sle.ast.typedPattern.ConstantRecord ->
                    ConstantRecord(expression.location, expression.type.apply(substitution), expression.fields.map { ConstantField(it.location, it.name, apply(it.value, substitution)) })

                is IdReference ->
                    IdReference(expression.location, expression.type.apply(substitution), expression.name)

                is LetExpression ->
                    LetExpression(expression.location, expression.type.apply(substitution), expression.declarations.map { apply(it, substitution) }, apply(expression.expression, substitution))

                is IfExpression ->
                    IfExpression(expression.location, expression.type.apply(substitution), apply(expression.guardExpression, substitution), apply(expression.thenExpression, substitution), apply(expression.elseExpression, substitution))

                is LambdaExpression ->
                    LambdaExpression(expression.location, expression.type.apply(substitution), apply(expression.argument, substitution), apply(expression.expression, substitution))

                is CallExpression ->
                    CallExpression(expression.location, expression.type.apply(substitution), apply(expression.operator, substitution), apply(expression.operand, substitution))

                is FieldProjectionExpression ->
                    FieldProjectionExpression(expression.location, expression.type.apply(substitution), apply(expression.record, substitution), expression.name)

                is CaseExpression ->
                    CaseExpression(expression.location, expression.type.apply(substitution), apply(expression.operator, substitution), expression.items.map { apply(it, substitution) })
            }


    private fun apply(caseItem: CaseItem, substitution: Substitution): CaseItem =
            CaseItem(caseItem.location, apply(caseItem.pattern, substitution), apply(caseItem.expression, substitution))


    private fun apply(pattern: Pattern, substitution: Substitution): Pattern =
            when (pattern) {
                is ConstantIntPattern ->
                    pattern

                is ConstantBoolPattern ->
                    pattern

                is ConstantStringPattern ->
                    pattern

                is ConstantCharPattern ->
                    pattern

                is ConstantUnitPattern ->
                    pattern

                is IdReferencePattern ->
                    IdReferencePattern(pattern.location, pattern.type.apply(substitution), pattern.name)

                is IgnorePattern ->
                    pattern

                is ConstructorReferencePattern ->
                    ConstructorReferencePattern(pattern.location, pattern.type.apply(substitution), pattern.name, pattern.parameters.map { apply(it, substitution) })
            }
}


private class SolverContext(private var varPump: VarPump, private var constraints: Constraints, private val environment: Environment) {
    val errors =
            mutableSetOf<Error>()


    private fun List<Type>.subst(substitution: Substitution): List<Type> =
            this.map { it.apply(substitution) }


    fun solve(): Substitution {
        var subst =
                Substitution()

        while (constraints.isNotEmpty()) {
            val constraint =
                    constraints[0]

            val u =
                    unifies(constraint.t1, constraint.t2)

            subst = u.first + subst
            constraints = u.second + constraints.drop(1).apply(u.first)
        }

        return subst
    }


    private fun unifies(t1: Type, t2: Type): Unifier =
            when {
                t1 == t2 ->
                    Pair(nullSubstitution, noConstraints)

                t1 is TCon && t2 is TCon && t1.name == t2.name && t1.arguments.size == t2.arguments.size ->
                    unifyMany(t1.arguments, t2.arguments)

                t1 is TAlias && t2 is TAlias && t1.name == t2.name && t1.arguments.size == t2.arguments.size ->
                    unifyMany(t1.arguments, t2.arguments)

                t1 is TVar ->
                    Pair(Substitution(t1.variable, t2), noConstraints)

                t2 is TVar ->
                    Pair(Substitution(t2.variable, t1), noConstraints)

                t1 is TArr && t2 is TArr ->
                    unifyMany(listOf(t1.domain, t1.range), listOf(t2.domain, t2.range))

                t1 is TRec && t2 is TRec -> {
                    val t1Map =
                            mapOf<String, Type>() + t1.fields

                    val t2Map =
                            mapOf<String, Type>() + t2.fields

                    if (t1.fixed && t2.fixed) {
                        if (t1.fields.size == t2.fields.size) {
                            if (t1Map.keys == t2Map.keys) {
                                unifyMany(t1Map.toSortedMap().values, t2Map.toSortedMap().values)
                            } else {
                                errors.add(RecordFieldNamesMismatch(t1, t2))

                                Pair(nullSubstitution, noConstraints)
                            }
                        } else {
                            errors.add(DifferingRecordSize(t1, t2))

                            Pair(nullSubstitution, noConstraints)
                        }
                    } else if (t1.fixed) {
                        if (t1Map.keys.containsAll(t2Map.keys)) {
                            unifyMany(t1Map.filterKeys { k -> t2Map.containsKey(k) }.toSortedMap().values, t2Map.toSortedMap().values)
                        } else {
                            errors.add(RecordFieldNamesMismatch(t1, t2))

                            Pair(nullSubstitution, noConstraints)
                        }
                    } else if (t2.fixed) {
                        if (t2Map.keys.containsAll(t1Map.keys)) {
                            unifyMany(t1Map.toSortedMap().values, t2Map.filterKeys { k -> t1Map.containsKey(k) }.toSortedMap().values)
                        } else {
                            errors.add(RecordFieldNamesMismatch(t1, t2))

                            Pair(nullSubstitution, noConstraints)
                        }
                    } else {
                        val keys =
                                t2Map.keys.intersect(t1Map.keys)

                        unifyMany(t1Map.filterKeys { k -> keys.contains(k) }.toSortedMap().values, t2Map.filterKeys { k -> keys.contains(k) }.toSortedMap().values)
                    }
                }

                t1 is TAlias -> {
                    val type =
                            environment.instantiateAlias(t1.name, varPump)!!

                    unifies(type, t2)
                }

                t2 is TAlias -> {
                    val type =
                            environment.instantiateAlias(t2.name, varPump)!!

                    unifies(t1, type)
                }

                else -> {
                    errors.add(UnificationFail(t1, t2))

                    Pair(nullSubstitution, noConstraints)
                }
            }


    private fun unifyMany(t1s: Collection<Type>, t2s: Collection<Type>): Unifier =
            when {
                t1s.isEmpty() && t2s.isEmpty() ->
                    Pair(nullSubstitution, noConstraints)

                t1s.isNotEmpty() && t2s.isNotEmpty() -> {
                    val t1 =
                            t1s.first()

                    val t2 =
                            t2s.first()

                    val u1 =
                            unifies(t1, t2)

                    val um =
                            unifyMany(t1s.drop(1).subst(u1.first), t2s.drop(1).subst(u1.first))


                    Pair(um.first + u1.first, u1.second + um.second)
                }

                else -> {
                    errors.add(UnificationMismatch(t1s, t2s))

                    Pair(nullSubstitution, noConstraints)
                }
            }
}


private fun Environment.instantiateAlias(name: QString, varPump: VarPump): Type? =
        this.alias(name)?.instantiate(varPump)
