package za.co.no9.sle.typing


sealed class Type {
    abstract fun apply(s: Substitution): Type

    abstract fun ftv(): Set<Var>
}


data class Variable(val name: Var, val constraint: Type?)


data class TVar(val variable: Var) : Type() {
    override fun apply(s: Substitution) =
            s[variable] ?: this

    override fun ftv() =
            setOf(variable)

    override fun toString(): String =
            "'$variable"
}


data class TCon(val name: String) : Type() {
    override fun apply(s: Substitution) =
            this

    override fun ftv() =
            emptySet<Var>()

    override fun toString(): String =
            name
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

val typeInt =
        TCon("Int")

val typeBool =
        TCon("Bool")

val typeString =
        TCon("String")


val builtInTypes =
        setOf(typeBool.name, typeError.name, typeInt.name, typeString.name)


