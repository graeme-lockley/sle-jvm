package za.co.no9.sle.typing

import za.co.no9.sle.inference.Constraints
import za.co.no9.sle.inference.noConstraints


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


    fun instantiate(varPump: VarPump): Pair<Type, Constraints> {
        val asP =
                parameters.map { varPump.fresh() }

        val substitution =
                Substitution(parameters.map { it.name }.zip(asP).toMap())

        return Pair(type.apply(substitution), noConstraints)
    }
}


data class Parameter(val name: Var, val constraint: Type?) {
    override fun toString(): String =
            if (constraint == null)
                "$name"
            else
                "$name: $constraint"
}
