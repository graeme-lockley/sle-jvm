package za.co.no9.sle.ast.pass3

import za.co.no9.sle.*


typealias Unifier =
        Pair<Subst, Constraints>


fun unifies(constraints: Constraints): Either<List<Error>, Subst> {
    val solver =
            Solver(constraints)

    val subst =
            solver.solve()

    return if (solver.errors.isEmpty())
        value(subst)
    else
        error(solver.errors)
}


private class Solver(private var constraints: Constraints) {
    val errors =
            mutableListOf<Error>()


    private fun Constraints.apply(subst: Subst): Constraints =
            this.map { Pair(it.first.apply(subst), it.second.apply(subst)) }


    private fun List<Type>.subst(subst: Subst): List<Type> =
            this.map { it.apply(subst) }


    fun solve(): Subst {
        var subst =
                nullSubst

        while (constraints.isNotEmpty()) {
            val constraint =
                    constraints[0]

            val u =
                    unifies(constraint.first, constraint.second)

            subst = compose(u.first, subst)
            constraints = u.second + constraints.drop(1).apply(u.first)
        }

        return subst
    }


    private fun unifies(t1: Type, t2: Type): Unifier =
            when {
                t1 == t2 ->
                    Pair(nullSubst, emptyList())

                t1 is TVar ->
                    Pair(mapOf(Pair(t1.variable, t2)), emptyList())

                t2 is TVar ->
                    Pair(mapOf(Pair(t2.variable, t1)), emptyList())

                t1 is TArr && t2 is TArr ->
                    unifyMany(listOf(t1.domain, t1.range), listOf(t2.domain, t2.range))

                else -> {
                    errors.add(UnificationFail(t1, t2))

                    Pair(nullSubst, emptyList())
                }
            }


    private fun unifyMany(t1s: List<Type>, t2s: List<Type>): Unifier =
            when {
                t1s.isEmpty() && t2s.isEmpty() ->
                    Pair(nullSubst, emptyList())

                t1s.isNotEmpty() && t2s.isNotEmpty() -> {
                    val t1 =
                            t1s[0]

                    val t2 =
                            t2s[0]

                    val u1 =
                            unifies(t1, t2)

                    val um =
                            unifyMany(t1s.drop(1).subst(u1.first), t2s.drop(1).subst(u1.first))

                    Pair(compose(um.first, u1.first), u1.second + um.second)
                }

                else -> {
                    errors.add(UnificationMismatch(t1s, t2s))

                    Pair(nullSubst, emptyList())
                }
            }
}