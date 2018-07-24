package za.co.no9.sle.typing


data class Schema(val parameters: List<Parameter>, val type: Type) {
    fun apply(s: Substitution): Schema =
            Schema(parameters, type.apply(s - parameters.map { it.name }))


    fun ftv() =
            type.ftv().minus(parameters)


    override fun toString(): String =
            if (parameters.isEmpty())
                type.toString()
            else
                "forall ${parameters.joinToString(", ") { it.toString() }} => $type"


    fun instantiate(varPump: VarPump): Type {
        val asP =
                parameters.map { varPump.fresh() }

        val substitution =
                Substitution(parameters.map { it.name }.zip(asP).toMap())

        return type.apply(substitution)
    }
}


data class Parameter(val name: Var, val constraint: Type?)
