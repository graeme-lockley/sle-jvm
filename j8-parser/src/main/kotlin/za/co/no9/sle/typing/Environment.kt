package za.co.no9.sle.typing


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