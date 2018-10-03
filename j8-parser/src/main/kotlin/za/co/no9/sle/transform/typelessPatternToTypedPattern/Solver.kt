package za.co.no9.sle.transform.typelessPatternToTypedPattern

import za.co.no9.sle.*
import za.co.no9.sle.ast.typedPattern.*
import za.co.no9.sle.ast.typedPattern.Unit
import za.co.no9.sle.typing.*


private typealias Unifier =
        Pair<Substitution, Constraints>


typealias Aliases =
        Map<String, Scheme>


fun unifies(varPump: VarPump, aliases: Aliases, constraints: Constraints): Either<Errors, Substitution> {
    val context =
            SolverContext(varPump, aliases, constraints)

    val subst =
            context.solve()

    return if (context.errors.isEmpty())
        value(subst)
    else
        error(context.errors)
}


fun Module.apply(substitution: Substitution): Module =
        Module(this.location, this.declarations.map {
            when (it) {
                is LetDeclaration ->
                    it.apply(substitution)

                is TypeAliasDeclaration ->
                    it

                is TypeDeclaration ->
                    it
            }
        })


fun LetDeclaration.apply(substitution: Substitution): LetDeclaration =
     LetDeclaration(this.location, this.scheme, this.name, this.expression.apply(substitution))


private fun Expression.apply(substitution: Substitution): Expression =
        when (this) {
            is Unit ->
                this

            is ConstantBool ->
                this

            is ConstantInt ->
                this

            is ConstantString ->
                this

            is IdReference ->
                IdReference(location, type.apply(substitution), name)

            is IfExpression ->
                IfExpression(location, type.apply(substitution), guardExpression.apply(substitution), thenExpression.apply(substitution), elseExpression.apply(substitution))

            is LambdaExpression ->
                LambdaExpression(location, type.apply(substitution), argument, expression.apply(substitution))

            is CallExpression ->
                CallExpression(location, type.apply(substitution), operator.apply(substitution), operand.apply(substitution))
        }


private class SolverContext(private var varPump: VarPump, private var aliases: Aliases, private var constraints: Constraints) {
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
                        if (t1 is TCon && !builtInTypes.contains(t1.name)) {
                            errors.add(UnknownType(t1.name))
                            Pair(nullSubstitution, noConstraints)
                        } else if (t2 is TCon && !builtInTypes.contains(t2.name)) {
                            errors.add(UnknownType(t2.name))
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