package za.co.no9.sle.typing


data class Schema(val variable: List<Var>, val type: Type) {
    fun apply(s: Substitution): Schema =
            Schema(variable, type.apply(s - variable))


    fun ftv() =
            type.ftv().minus(variable)


    override fun toString(): String =
            if (variable.isEmpty())
                type.toString()
            else
                "forall ${variable.joinToString(", ") { it.toString() }} => $type"


    fun instantiate(varPump: VarPump): Type {
        val asP =
                variable.map { varPump.fresh() }

        val substitution =
                Substitution(variable.zip(asP).toMap())

        return type.apply(substitution)
    }
}