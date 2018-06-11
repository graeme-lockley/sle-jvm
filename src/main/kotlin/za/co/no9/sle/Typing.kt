package za.co.no9.sle


typealias Subst =
        Map<String, Type>

val nullSubst =
        emptyMap<String, Type>()

fun compose(s1: Subst, s2: Subst): Subst =
        s2.plus(s1)


sealed class Type {
    abstract fun apply(s: Subst): Type

    abstract fun ftv(): Set<String>
}

data class TVar(private val name: String) : Type() {
    override fun apply(s: Subst) =
            s.getOrDefault(name, this)

    override fun ftv() =
            setOf(name)
}

data class TCon(private val name: String) : Type() {
    override fun apply(s: Subst) =
            this

    override fun ftv() =
            emptySet<String>()
}

data class TArr(private val domain: Type, private val range: Type) : Type() {
    override fun apply(s: Subst) =
            TArr(domain.apply(s), range.apply(s))

    override fun ftv() =
            domain.ftv().plus(range.ftv())
}


val typeInt =
        TCon("Int")

val typeBool =
        TCon("Bool")

val typeString =
        TCon("String")


data class Schema(private val variable: List<String>, private val type: Type) {
    fun apply(s: Subst): Schema =
            Schema(variable, type.apply(s.minus(variable)))

    fun ftv(): Set<String> =
            type.ftv().minus(variable)
}


data class TypeEnv(private val env: Map<String, Schema>) {
    fun extend(k: String, s: Schema): TypeEnv =
            TypeEnv(env.plus(Pair(k, s)))

    fun apply(s: Subst): TypeEnv =
            TypeEnv(env.mapValues { it.value.apply(s) })

    fun ftv(): Set<String> =
            env.values.map{ it.ftv() }.fold(emptySet()) { s1, s2 -> s1.plus(s2) }
}
