package za.co.no9.sle.typing


sealed class Binding

data class SchemeBinding(
        val scheme: Schema): Binding()

data class TypeBinding(
        val type: Type): Binding()


data class Environment(val state: Map<String, Binding> = mapOf()) {
    operator fun get(name: String): Schema? {
        val binding =
        state[name]

        return if (binding == null) {
            null
        } else {
            when (binding) {
                is SchemeBinding ->
                    binding.scheme

                else ->
                    null
            }
        }
    }

    fun set(name: String, schema: Schema): Environment =
            Environment(this.state + Pair(name, SchemeBinding(schema)))

    fun set(name: String, type: Type): Environment =
            Environment(this.state + Pair(name, TypeBinding(type)))

    fun containsKey(name: String): Boolean =
            state.containsKey(name)
}


val emptyEnvironment =
        Environment()