package za.co.no9.sle.typing

import za.co.no9.sle.Location
import za.co.no9.sle.QString
import za.co.no9.sle.homeLocation


sealed class Type(open val location: Location) {
    abstract fun apply(s: Substitution): Type


    abstract fun ftv(): Set<Var>
}


data class TVar(
        override val location: Location,
        val variable: Var) : Type(location) {

    override fun apply(s: Substitution) =
            s[variable] ?: this


    override fun ftv() =
            setOf(variable)


    override fun toString(): String =
            "'$variable"
}


data class TAlias(
        override val location: Location,
        val name: QString,
        val arguments: List<Type> = emptyList()) : Type(location) {

    override fun apply(s: Substitution) =
            if (arguments.isEmpty())
                this
            else
                TAlias(location, name, arguments.map { it.apply(s) })


    override fun ftv() =
            arguments.fold(emptySet<Var>()) { ftvs, type -> ftvs + type.ftv() }


    override fun toString(): String =
            if (arguments.isEmpty())
                "alias.$name"
            else
                "alias.$name ${arguments.joinToString(" ") { it.toString() }}"
}


data class TCon(
        override val location: Location,
        val name: String,
        val arguments: List<Type> = emptyList()) : Type(location) {

    override fun apply(s: Substitution) =
            if (arguments.isEmpty())
                this
            else
                TCon(location, name, arguments.map { it.apply(s) })


    override fun ftv() =
            arguments.fold(emptySet<Var>()) { ftvs, type -> ftvs + type.ftv() }


    override fun toString(): String =
            if (name == "Tuple" || name == "Tuple${arguments.size}")
                "(${arguments.joinToString(", ") { it.toString() }})"
            else if (arguments.isEmpty())
                name
            else
                "$name ${arguments.joinToString(" ") { it.toString() }}"
}


data class TArr(
        override val location: Location,
        val domain: Type,
        val range: Type) : Type(location) {

    constructor(domain: Type, range: Type) : this(domain.location + range.location, domain, range)


    override fun apply(s: Substitution) =
            TArr(location, domain.apply(s), range.apply(s))


    override fun ftv() =
            domain.ftv().plus(range.ftv())


    override fun toString(): String =
            when (domain) {
                is TArr ->
                    "($domain) -> $range"
                else ->
                    "$domain -> $range"
            }
}


val typeError =
        TCon(homeLocation, "Data.Error")

val typeUnit =
        TCon(homeLocation, "Data.Unit")

val typeInt =
        TCon(homeLocation, "Data.Int")

val typeBool =
        TCon(homeLocation, "Data.Bool")

val typeString =
        TCon(homeLocation, "Data.String")

val typeChar =
        TCon(homeLocation, "Data.Char")


