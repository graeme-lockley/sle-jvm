package za.co.no9.sle


typealias Var =
        Int

typealias Subst =
        Map<Var, Type>


val nullSubst: Subst =
        emptyMap()

fun compose(s1: Subst, s2: Subst): Subst =
        s2.plus(s1)


sealed class Type {
    abstract fun apply(s: Subst): Type

    abstract fun ftv(): Set<Var>
}

data class TVar(private val variable: Var) : Type() {
    override fun apply(s: Subst) =
            s.getOrDefault(variable, this)

    override fun ftv() =
            setOf(variable)
}

data class TCon(private val name: String) : Type() {
    override fun apply(s: Subst) =
            this

    override fun ftv() =
            emptySet<Var>()
}

data class TArr(private val domain: Type, private val range: Type) : Type() {
    override fun apply(s: Subst) =
            TArr(domain.apply(s), range.apply(s))

    override fun ftv() =
            domain.ftv().plus(range.ftv())
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
    fun apply(s: Subst): Schema =
            Schema(variable, type.apply(s.minus(variable)))

    fun ftv() =
            type.ftv().minus(variable)
}


data class TypeEnv(private val env: Map<Var, Schema>) {
    fun extend(k: Var, s: Schema): TypeEnv =
            TypeEnv(env.plus(Pair(k, s)))

    fun apply(s: Subst): TypeEnv =
            TypeEnv(env.mapValues { it.value.apply(s) })

    fun ftv(): Set<Var> =
            env.values.map { it.ftv() }.fold(emptySet()) { s1, s2 -> s1.plus(s2) }
}


typealias Environment =
        Map<String, Schema>


fun Environment.lookup(name: String): Schema? =
        this[name]

fun Environment.bindInScope(name: String, schema: Schema): Environment =
        this + Pair(name, schema)


val emptyEnvironment =
        mapOf<String, Schema>()