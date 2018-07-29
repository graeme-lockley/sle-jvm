package za.co.no9.sle.typing

import za.co.no9.sle.inference.Constraint
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

        val constraints =
                parameters.zip(asP).fold(noConstraints) { constraints, (parameter, varName) ->
                    val parameterConstraint =
                            parameter.constraint

                    if (parameterConstraint == null)
                        constraints
                    else
                        constraints + Constraint(varName, parameterConstraint.apply(substitution))
                }

        return Pair(type.apply(substitution), constraints)
    }
}


data class Parameter(val name: Var, val constraint: Type?) {
    override fun toString(): String =
            if (constraint == null)
                "$name"
            else
                "$name: $constraint"
}


private fun isConstraint(type: Type): Boolean =
        when (type) {
            is TVar ->
                true

            is TCon ->
                false

            is TArr ->
                isConstraint(type.domain) || isConstraint(type.range)

            is TOr ->
                true
        }


fun generalise(type: Type, substitution: Substitution = nullSubstitution): Schema {
    val typeFtv =
            type.ftv().toList()

    val subParameters =
            typeFtv.map { TVar(it).apply(substitution) }

    val substitutionMap =
            typeFtv.zip(subParameters).filter { !isConstraint(it.second) }.fold(nullSubstitution) { s, m -> s + Substitution(m.first, m.second) }

    return Schema(typeFtv.zip(subParameters).filter { isConstraint(it.second) }.map { Parameter(it.first, if (it.second == TVar(it.first)) null else it.second) }, type.apply(substitutionMap))
}