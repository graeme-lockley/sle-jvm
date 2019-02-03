package za.co.no9.sle.typing

import za.co.no9.sle.Location
import za.co.no9.sle.QString
import za.co.no9.sle.homeLocation
import za.co.no9.sle.location


sealed class Type(open val location: Location) {
    abstract fun apply(s: Substitution): Type


    abstract fun ftv(): Set<Var>
}


data class TVar(
        override val location: Location,
        val variable: Var) : Type(location) {

    constructor(variable: Var) : this(homeLocation, variable)

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

    constructor(name: QString, arguments: List<Type>) : this(homeLocation, name, arguments)

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
            when {
                name == "Tuple${arguments.size}" ->
                    "(${arguments.joinToString(", ") { it.toString() }})"

                arguments.isEmpty() ->
                    name

                else ->
                    "$name ${arguments.joinToString(" ") { it.toString() }}"
            }
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


data class TRec(
        override val location: Location,
        val fixed: Boolean,
        val fields: List<Pair<String, Type>>) : Type(location) {

    constructor(fixed: Boolean, fields: List<Pair<String, Type>>) : this(location(fields.map { it.second.location })
            ?: homeLocation, fixed, fields)


    override fun apply(s: Substitution) =
            TRec(location, fixed, fields.map { Pair(it.first, it.second.apply(s)) })


    override fun ftv() =
            fields.fold(emptySet<Var>()) { a, b -> a + b.second.ftv() }


    override fun toString(): String =
            when {
                fixed ->
                    "{${fields.joinToString(", ") { "${it.first} : ${it.second}" }}}"

                fields.isEmpty() ->
                    "{..}"

                else ->
                    "{${fields.joinToString(", ") { "${it.first} : ${it.second}" }}, ..}"
            }
}


fun similar(t1: Type, t2: Type): Boolean =
        when {
            t1 is TCon && t2 is TCon ->
                t1.name == t2.name &&
                        t1.arguments.zip(t2.arguments).fold(true) { a, b -> a && similar(b.first, b.second) }

            t1 is TVar && t2 is TVar ->
                t1.variable == t2.variable

            t1 is TAlias && t2 is TAlias ->
                t1.name == t2.name &&
                        t1.arguments.zip(t2.arguments).fold(true) { a, b -> a && similar(b.first, b.second) }

            t1 is TArr && t2 is TArr ->
                similar(t1.domain, t2.domain) && similar(t1.range, t2.range)

            else ->
                false
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


