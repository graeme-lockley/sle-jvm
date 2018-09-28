package za.co.no9.sle.typing


sealed class Type {
    abstract fun apply(s: Substitution): Type

    abstract fun ftv(): Set<Var>
}


data class TVar(val variable: Var) : Type() {
    override fun apply(s: Substitution) =
            s[variable] ?: this

    override fun ftv() =
            setOf(variable)

    override fun toString(): String =
            "'$variable"
}


data class TCon(val name: String, val arguments: List<Type> = emptyList()) : Type() {
    override fun apply(s: Substitution) =
            if (arguments.isEmpty())
                this
            else
                TCon(name, arguments.map { it.apply(s) })

    override fun ftv() =
            arguments.fold(emptySet<Var>()) { ftvs, type -> ftvs + type.ftv() }

    override fun toString(): String =
            if (arguments.isEmpty())
                name
            else
                "$name ${arguments.map { it.toString() }.joinToString(" ")}"
}


data class TArr(val domain: Type, val range: Type) : Type() {
    override fun apply(s: Substitution) =
            TArr(domain.apply(s), range.apply(s))

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
        TCon(":Error:")

val typeUnit =
        TCon("()")

val typeInt =
        TCon("Int")

val typeBool =
        TCon("Bool")

val typeString =
        TCon("String")


val builtInTypes =
        setOf(typeUnit.name, typeBool.name, typeError.name, typeInt.name, typeString.name)


