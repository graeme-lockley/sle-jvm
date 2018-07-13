package za.co.no9.sle.pass3

import za.co.no9.sle.*
import za.co.no9.sle.typing.*


typealias Unifier =
        Pair<Substitution, Constraints>


fun unifies(constraints: Constraints): Either<List<Error>, Substitution> {
    val context =
            SolverContext(constraints)

    val subst =
            context.solve()

    return if (context.errors.isEmpty())
        value(subst)
    else
        error(context.errors)
}


fun za.co.no9.sle.pass2.Module.assignTypesToCoreAST(environment: Environment): Either<Errors, Module> =
        infer(this, environment)
                .andThen { pair -> unifies(pair.second).map { Pair(pair.first, it) } }
                .map { it.first.apply(it.second) }


fun Module.apply(substitution: Substitution): Module =
        Module(this.location, this.declarations.map {
            when (it) {
                is LetDeclaration ->
                    LetDeclaration(it.location, it.type.apply(substitution), it.name, it.expression.apply(substitution))

                is TypeAliasDeclaration ->
                        it
            }
        })


fun Expression.apply(substitution: Substitution): Expression =
        when (this) {
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
                CallExpression(location, type.apply(substitution), operand.apply(substitution), operator.apply(substitution))
        }


private class SolverContext(private var constraints: Constraints) {
    val errors =
            mutableListOf<Error>()


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

                t1 is TVar ->
                    Pair(Substitution(t1.variable, t2), noConstraints)

                t2 is TVar ->
                    Pair(Substitution(t2.variable, t1), noConstraints)

                t1 is TArr && t2 is TArr ->
                    unifyMany(listOf(t1.domain, t1.range), listOf(t2.domain, t2.range))

                else -> {
                    errors.add(UnificationFail(t1, t2))

                    Pair(nullSubstitution, noConstraints)
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