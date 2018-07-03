package za.co.no9.sle


typealias Var =
        Int


data class Substitution(val state: Map<Var, Type> = emptyMap()) {
    constructor(key: Var, value: Type) : this(mapOf(Pair(key, value)))

    operator fun plus(other: Substitution): Substitution =
            Substitution(other.state.mapValues { it.value.apply(this) } + state)

    operator fun get(key: Var): Type? =
            state[key]

    operator fun minus(keys: List<Var>): Substitution =
            Substitution(state - keys)

    override fun toString(): String =
            state.entries.map { "'${it.key} ${it.value}" }.sorted().joinToString(", ")
}


val nullSubstitution: Substitution =
        Substitution()


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


data class TypeEnv(private val env: Map<Var, Schema>) {
    fun extend(k: Var, s: Schema): TypeEnv =
            TypeEnv(env.plus(Pair(k, s)))

    fun apply(s: Substitution): TypeEnv =
            TypeEnv(env.mapValues { it.value.apply(s) })

    fun ftv(): Set<Var> =
            env.values.map { it.ftv() }.fold(emptySet()) { s1, s2 -> s1.plus(s2) }
}


data class Environment(val state: Map<String, Schema> = mapOf()) {
    operator fun get(name: String): Schema? =
            state[name]

    fun set(name: String, schema: Schema): Environment =
            Environment(this.state + Pair(name, schema))

    fun containsKey(name: String): Boolean =
            state.containsKey(name)
}


val emptyEnvironment =
        Environment()