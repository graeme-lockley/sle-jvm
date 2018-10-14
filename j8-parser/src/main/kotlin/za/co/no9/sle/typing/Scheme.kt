package za.co.no9.sle.typing


data class Scheme(val parameters: List<Var>, val type: Type) {
    fun apply(s: Substitution): Scheme =
            Scheme(parameters, type.apply(s - parameters))


    fun ftv() =
            type.ftv().minus(parameters)


    override fun toString(): String =
            "<${parameters.joinToString(", ") { it.toString() }}> $type"


    fun instantiate(varPump: VarPump): Type {
        val asP =
                parameters.map { varPump.fresh(type.location) }

        val substitution =
                Substitution(parameters.zip(asP).toMap())

        return type.apply(substitution)
    }

    fun normalize(): Scheme {
        val subs =
                Substitution(parameters.foldIndexed(emptyMap()) { a, b, c -> b.plus(Pair(c, TVar(type.location, a))) })

        return Scheme(
                parameters.mapIndexed { index, _ -> index },
                type.apply(subs)
        )
    }

    fun isCompatibleWith(other: Scheme): Boolean {
        val normalizedThis =
                this.normalize()

        val noramlizedOther =
                other.normalize()

        return normalizedThis.toString() == noramlizedOther.toString()
    }
}


fun generalise(type: Type, substitution: Substitution = nullSubstitution): Scheme {
    val typeFtv =
            type.ftv().toList()

    val substitutionParameters =
            typeFtv.map { TVar(type.location, it).apply(substitution) }

    val typeSubstitution =
            typeFtv.zip(substitutionParameters).map { Substitution(it.first, it.second) }.fold(nullSubstitution) { s, m -> s + m }

    val type1 = type.apply(typeSubstitution)

    return Scheme(type1.ftv().toList(), type1)
}