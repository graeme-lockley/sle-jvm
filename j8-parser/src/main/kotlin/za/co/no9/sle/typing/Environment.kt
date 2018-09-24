package za.co.no9.sle.typing


sealed class Binding

data class SchemeBinding(
        val scheme: Scheme) : Binding()

data class TypeBinding(
        val type: Type) : Binding()


data class Environment(val state: Map<String, Binding> = mapOf()) {
    fun scheme(name: String): Scheme? {
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

    fun set(name: String, scheme: Scheme): Environment =
            Environment(this.state + Pair(name, SchemeBinding(scheme)))

    fun set(name: String, type: Type): Environment =
            Environment(this.state + Pair(name, TypeBinding(type)))

    fun containsKey(name: String): Boolean =
            state.containsKey(name)
}


val emptyEnvironment =
        Environment()