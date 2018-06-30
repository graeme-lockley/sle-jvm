package za.co.no9.sle.ast.pass3

import za.co.no9.sle.*


typealias Unifier =
        Pair<Substitution, Constraints>


fun unifies(constraints: Constraints): Either<List<Error>, Substitution> {
    val solver =
            Solver(constraints)

    val subst =
            solver.solve()

    return if (solver.errors.isEmpty())
        value(subst)
    else
        error(solver.errors)
}


fun apply(substitution: Substitution, module: Module): Module =
        Module(module.location, module.declarations.map {
            when(it) {
                is LetDeclaration ->
                        LetDeclaration(it.location, it.type.apply(substitution), it.name, apply(substitution, it.expression))
            }
        })


fun apply(substitution: Substitution, expression: Expression): Expression =
        when (expression) {
            is ConstantBool ->
                expression

            is ConstantInt ->
                expression

            is ConstantString ->
                expression

            is IdReference ->
                IdReference(expression.location, expression.type.apply(substitution), expression.name)

            is IfExpression ->
                IfExpression(expression.location, expression.type.apply(substitution), apply(substitution, expression.guardExpression), apply(substitution, expression.thenExpression), apply(substitution, expression.elseExpression))

            is LambdaExpression ->
                LambdaExpression(expression.location, expression.type.apply(substitution), expression.argument, apply(substitution, expression.expression))

            is CallExpression ->
                CallExpression(expression.location, expression.type.apply(substitution), apply(substitution, expression.operand), apply(substitution, expression.operator))
        }


private class Solver(private var constraints: Constraints) {
    val errors =
            mutableListOf<Error>()


    private fun Constraints.apply(substitution: Substitution): Constraints =
            this.map { Pair(it.first.apply(substitution), it.second.apply(substitution)) }


    private fun List<Type>.subst(substitution: Substitution): List<Type> =
            this.map { it.apply(substitution) }


    fun solve(): Substitution {
        var subst =
                Substitution()

        while (constraints.isNotEmpty()) {
            val constraint =
                    constraints[0]

            val u =
                    unifies(constraint.first, constraint.second)

            subst = u.first +  subst
            constraints = u.second + constraints.drop(1).apply(u.first)
        }

        return subst
    }


    private fun unifies(t1: Type, t2: Type): Unifier =
            when {
                t1 == t2 ->
                    Pair(nullSubstitution, emptyList())

                t1 is TVar ->
                    Pair(Substitution(t1.variable, t2), emptyList())

                t2 is TVar ->
                    Pair(Substitution(t2.variable, t1), emptyList())

                t1 is TArr && t2 is TArr ->
                    unifyMany(listOf(t1.domain, t1.range), listOf(t2.domain, t2.range))

                else -> {
                    errors.add(UnificationFail(t1, t2))

                    Pair(nullSubstitution, emptyList())
                }
            }


    private fun unifyMany(t1s: List<Type>, t2s: List<Type>): Unifier =
            when {
                t1s.isEmpty() && t2s.isEmpty() ->
                    Pair(nullSubstitution, emptyList())

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

                    Pair(nullSubstitution, emptyList())
                }
            }
}