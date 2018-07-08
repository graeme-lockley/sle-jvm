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
                "forall ${variable.map { it.toString() }.joinToString(", ")} => $type"
}