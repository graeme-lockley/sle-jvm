package za.co.no9.sle.typing


data class Schema(val variables: List<Variable>, val type: Type) {
    fun apply(s: Substitution): Schema =
            Schema(variables, type.apply(s - variables.map { it.name }))


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
                Substitution(variables.map { it.name }.zip(asP).toMap())

        return type.apply(substitution)
    }
}
