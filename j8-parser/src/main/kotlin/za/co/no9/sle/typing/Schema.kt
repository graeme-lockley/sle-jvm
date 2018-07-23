package za.co.no9.sle.typing


data class Schema(val variables: List<Var>, val type: Type) {
    fun apply(s: Substitution): Schema =
            Schema(variables, type.apply(s - variables))


    fun ftv() =
            type.ftv().minus(variables)


    override fun toString(): String =
            if (variables.isEmpty())
                type.toString()
            else
                "forall ${variables.joinToString(", ") { it.toString() }} => $type"


    fun instantiate(varPump: VarPump): Type {
        val asP =
                variables.map { varPump.fresh() }

        val substitution =
                Substitution(variables.zip(asP).toMap())

        return type.apply(substitution)
    }
}