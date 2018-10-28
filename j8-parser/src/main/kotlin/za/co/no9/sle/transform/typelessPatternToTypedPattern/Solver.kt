package za.co.no9.sle.transform.typelessPatternToTypedPattern

import za.co.no9.sle.*
import za.co.no9.sle.ast.typedPattern.*
import za.co.no9.sle.ast.typedPattern.Unit
import za.co.no9.sle.typing.*


private typealias Unifier =
        Pair<Substitution, Constraints>


typealias Aliases =
        Map<String, Scheme>


fun unifies(varPump: VarPump, aliases: Aliases, constraints: Constraints, environment: Environment): Either<Errors, Substitution> {
    val context =
            SolverContext(varPump, aliases, constraints, environment)

    val subst =
            context.solve()

    return if (context.errors.isEmpty())
        value(subst)
    else
        error(context.errors)
}


fun Module.apply(aliases: Aliases, substitution: Substitution): Either<Errors, Module> {
    val applyContext =
            ApplyContext(aliases)

    val module =
            applyContext.apply(this, substitution)

    return if (applyContext.errors.isEmpty())
        value(module)
    else
        error(applyContext.errors)
}


fun LetDeclaration.apply(aliases: Aliases, substitution: Substitution): Either<Errors, LetDeclaration> {
    val applyContext =
            ApplyContext(aliases)

    val module =
            applyContext.apply(this, substitution)

    return if (applyContext.errors.isEmpty())
        value(module)
    else
        error(applyContext.errors)
}


private class ApplyContext(private val aliases: Aliases) {
    val errors =
            mutableSetOf<Error>()


    fun apply(module: Module, substitution: Substitution): Module =
            Module(module.location, module.declarations.map {
                when (it) {
                    is LetDeclaration ->
                        apply(it, substitution)

                    is TypeAliasDeclaration ->
                        it

                    is TypeDeclaration ->
                        it
                }
            })


    fun apply(letDeclaration: LetDeclaration, substitution: Substitution): LetDeclaration {
        val appliedExpression =
                apply(letDeclaration.expression, substitution)

        val other =
                generalise(appliedExpression.type).normalize()

        if (!letDeclaration.scheme.expandAliases(aliases).isCompatibleWith(other.expandAliases(aliases))) {
            errors.add(IncompatibleDeclarationSignature(letDeclaration.location, letDeclaration.name.name, letDeclaration.scheme, other))
        }

        return LetDeclaration(letDeclaration.location, letDeclaration.scheme, letDeclaration.name, appliedExpression)
    }


    private fun Scheme.expandAliases(aliases: Aliases): Scheme =
            Scheme(parameters, type.expandAliases(aliases))


    private fun Type.expandAliases(aliases: Aliases): Type =
            when (this) {
                is TVar ->
                    this

                is TCon -> {
                    val alias =
                            aliases[name]

                    when {
                        alias == null ->
                            this

                        arguments.size != alias.parameters.size -> {
                            val emptyLocation =
                                    Location(Position(0, 0))

                            // TODO Pass in the type's actual position rather than creating an emptyLocation
                            errors.add(IncorrectNumberOfAliasArguments(emptyLocation, name, alias.parameters.size, arguments.size))
                            this
                        }

                        else -> {
                            val substitutionMap =
                                    alias.parameters.zip(arguments).fold(emptyMap<Var, Type>()) { a, b -> a + b }

                            alias.type.apply(Substitution(substitutionMap)).expandAliases(aliases)
                        }
                    }
                }

                is TArr ->
                    TArr(domain.expandAliases(aliases), range.expandAliases(aliases))
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

                is IdReference ->
                    IdReference(expression.location, expression.type.apply(substitution), expression.name)

                is IfExpression ->
                    IfExpression(expression.location, expression.type.apply(substitution), apply(expression.guardExpression, substitution), apply(expression.thenExpression, substitution), apply(expression.elseExpression, substitution))

                is LambdaExpression ->
                    LambdaExpression(expression.location, expression.type.apply(substitution), apply(expression.argument, substitution), apply(expression.expression, substitution))

                is CallExpression ->
                    CallExpression(expression.location, expression.type.apply(substitution), apply(expression.operator, substitution), apply(expression.operand, substitution))

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

                is ConstantUnitPattern ->
                    pattern

                is IdReferencePattern ->
                    IdReferencePattern(pattern.location, pattern.type.apply(substitution), pattern.name)

                is ConstructorReferencePattern ->
                    ConstructorReferencePattern(pattern.location, pattern.type.apply(substitution), pattern.name, pattern.parameters.map { apply(it, substitution) })
            }
}


private class SolverContext(private var varPump: VarPump, private var aliases: Aliases, private var constraints: Constraints, private val environment: Environment) {
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

                t1 is TVar ->
                    Pair(Substitution(t1.variable, t2), noConstraints)

                t2 is TVar ->
                    Pair(Substitution(t2.variable, t1), noConstraints)

                t1 is TArr && t2 is TArr ->
                    unifyMany(listOf(t1.domain, t1.range), listOf(t2.domain, t2.range))

                else -> {
                    if (t1 is TCon && aliases.containsKey(t1.name)) {
                        val type =
                                aliases[t1.name]!!.instantiate(varPump)

                        unifies(type, t2)
                    } else if (t2 is TCon && aliases.containsKey(t2.name)) {
                        val type =
                                aliases[t2.name]!!.instantiate(varPump)

                        unifies(t1, type)
                    } else {
                        if (t1 is TCon && !environment.containsType(t1.name)) {
                            errors.add(UnknownType(t1.location, t1.name))
                            Pair(nullSubstitution, noConstraints)
                        } else if (t2 is TCon && !environment.containsType(t2.name)) {
                            errors.add(UnknownType(t2.location, t2.name))
                            Pair(nullSubstitution, noConstraints)

                        } else {
                            errors.add(UnificationFail(t1, t2))

                            Pair(nullSubstitution, noConstraints)
                        }
                    }
                }
            }


    private fun unifyMany(t1s: List<Type>, t2s: List<Type>): Unifier =
            when {
                t1s.isEmpty() && t2s.isEmpty() ->
                    Pair(nullSubstitution, noConstraints)

                t1s.isNotEmpty() && t2s.isNotEmpty() -> {
                    val t1 =
                            t1s[0]

                    val t2 =
                            t2s[0]

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