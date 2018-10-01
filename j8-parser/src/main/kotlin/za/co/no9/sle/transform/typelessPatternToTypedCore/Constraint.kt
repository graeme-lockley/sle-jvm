package za.co.no9.sle.transform.typelessPatternToTypedCore

import za.co.no9.sle.typing.Substitution
import za.co.no9.sle.typing.Type

data class Constraint(val t1: Type, val t2: Type) {
    fun apply(substitution: Substitution): Constraint =
            Constraint(t1.apply(substitution), t2.apply(substitution))

    override fun toString(): String =
            "$t1 : $t2"
}