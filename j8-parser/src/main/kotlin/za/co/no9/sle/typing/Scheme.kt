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


private fun isConstraint(type: Type): Boolean =
        when (type) {
            is TVar ->
                true

            is TCon ->
                false

            is TArr ->
                isConstraint(type.domain) || isConstraint(type.range)
        }


fun generalise(type: Type, substitution: Substitution = nullSubstitution): Scheme {
    val typeFtv =
            type.ftv().toList()

    val substitutionParameters =
            typeFtv.map { TVar(it).apply(substitution) }

    val typeSubstitution =
            typeFtv.zip(substitutionParameters).filter { !isConstraint(it.second) }.map { Substitution(it.first, it.second) }.fold(nullSubstitution) { s, m -> s + m }

    return Scheme(typeFtv.zip(substitutionParameters).filter { isConstraint(it.second) }.map { it.first }, type.apply(typeSubstitution))
}