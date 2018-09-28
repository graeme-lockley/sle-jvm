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
                parameters.map { varPump.fresh() }

        val substitution =
                Substitution(parameters.zip(asP).toMap())

        return type.apply(substitution)
    }
}


fun generalise(type: Type, substitution: Substitution = nullSubstitution): Scheme {
    val typeFtv =
            type.ftv().toList()

    val substitutionParameters =
            typeFtv.map { TVar(it).apply(substitution) }

    val typeSubstitution =
            typeFtv.zip(substitutionParameters).map { Substitution(it.first, it.second) }.fold(nullSubstitution) { s, m -> s + m }

    val type1 = type.apply(typeSubstitution)

    return Scheme(type1.ftv().toList(), type1)
}