package za.co.no9.sle.transform.typelessPatternToTypedPattern

import za.co.no9.sle.typing.*

data class Constraints(val state: List<Constraint> = emptyList()) {
    operator fun plus(constraint: Constraint): Constraints =
            Constraints(state + constraint)

    operator fun plus(constraints: Constraints): Constraints =
            Constraints(state + constraints.state)

    fun apply(substitution: Substitution): Constraints =
            Constraints(state.map { it.apply(substitution) })

    fun isNotEmpty(): Boolean =
            state.isNotEmpty()

    operator fun get(index: Int): Constraint =
            state[index]

    fun drop(count: Int): Constraints =
            Constraints(state.drop(count))

    override fun toString(): String =
            state.joinToString(", ") { it.toString() }

    fun leftVar(): Constraints =
            Constraints(state.map {
                when {
                    it.t1 is TVar ->
                        it

                    it.t2 is TVar ->
                        Constraint(it.t2, it.t1)

                    else ->
                        it
                }
            })


    fun merge(env: Environment): Constraints {
        val constraints =
                state.toMutableList()

        var lp = 0
        while (lp < constraints.size) {
            val current =
                    constraints[lp]

            if (current.t1 is TVar) {
                var inner = lp + 1

                while (inner < constraints.size) {
                    val innerConstraint =
                            constraints[inner]

                    if (innerConstraint.t1 == current.t1) {
                        val mergeResult =
                                merge(resolveAlias(env, current.t2), resolveAlias(env, innerConstraint.t2))

                        if (mergeResult == null) {
                            val x1 = resolveAlias(env, current.t2)
                            val x2 = resolveAlias(env, innerConstraint.t2)
                            val x3 =
                                    merge(x1, x2)
                            println("Error - can't merge: $x1   $x2   $x3")
                            inner += 1
                        } else {
                            constraints[lp] = Constraint(current.t1, mergeResult.second)
                            constraints.removeAt(inner)

                            if (mergeResult.first != nullSubstitution) {
                                var l = 0
                                while (l < constraints.size) {

                                    fun ff(type: Type): Type =
                                            type.apply(mergeResult.first)

                                    constraints[l] = Constraint(ff(constraints[l].t1), ff(constraints[l].t2))

                                    l += 1
                                }

                                constraints += mergeResult.first.state.map { Constraint(TVar(it.key), it.value) }
                            }
                        }
                    } else {
                        inner += 1
                    }
                }
            }

            lp += 1
        }

        return Constraints(constraints)
    }


    private fun merge(t1: Type?, t2: Type?): Pair<Substitution, Type>? =
            when {
                t1 == null || t2 == null ->
                    null

                similar(t1, t2) ->
                    Pair(nullSubstitution, t1)

                t1 is TRec && !t1.fixed && t2 is TRec -> {
                    val t1Map =
                            t1.fields.toMap()

                    val t2Map =
                            t2.fields.toMap()

                    val commonFieldNames =
                            t1Map.keys.intersect(t2Map.keys)


                    val initial: Pair<Substitution?, List<Pair<String, Type>>> =
                            Pair(nullSubstitution, emptyList())

                    val zippedCombined =
                            t1Map.filter { commonFieldNames.contains(it.key) }.toSortedMap().toList().zip(
                                    t2Map.filter { commonFieldNames.contains(it.key) }.toSortedMap().toList())

                    val combined =
                            zippedCombined.fold(initial) { a, b ->
                                Pair(
                                        combine(a.first, createSubstitution(b.first.second, b.second.second)),
                                        a.second + b.first
                                )
                            }

                    val substitution =
                            combined.first

                    if (substitution == null) {
                        null
                    } else {
                        val t1Fields =
                                t1Map.filter { !commonFieldNames.contains(it.key) }.toList()

                        val t2Fields =
                                t2Map.filter { !commonFieldNames.contains(it.key) }.toList()

                        val combinedFields =
                                combined.second

                        Pair(
                                substitution,
                                TRec(t2.fixed, t1Fields + t2Fields + combinedFields)
                        )
                    }
                }

                else -> {
                    val substitution =
                            createSubstitution(t1, t2)

                    if (substitution == null)
                        null
                    else
                        Pair(substitution, t1)
                }
            }


    private fun createSubstitution(t1: Type, t2: Type): Substitution? =
            when {
                t1 == t2 ->
                    nullSubstitution

                t1 is TArr && t2 is TArr ->
                    combine(createSubstitution(t1.domain, t2.domain), createSubstitution(t1.range, t2.range))

                t1 is TVar && t2 is TVar ->
                    Substitution(t2.variable, t1)

                t1 is TCon && t2 is TCon && t1.name == t2.name && t1.arguments.size == t2.arguments.size -> {
                    val initial: Substitution? =
                            nullSubstitution

                    t1.arguments.zip(t2.arguments).fold(initial) { a, b ->
                        combine(a, createSubstitution(b.first, b.second))
                    }
                }

                t1 is TVar ->
                    Substitution(t1.variable, t2)

                t2 is TVar ->
                    Substitution(t2.variable, t1)

                else ->
                    null
            }


    private fun combine(s1: Substitution?, s2: Substitution?): Substitution? =
            when {
                s1 == null ->
                    s2

                s2 == null ->
                    s1

                else ->
                    s1 + s2
            }
}


val noConstraints =
        Constraints()