package za.co.no9.sle.typing


sealed class Binding

data class ValueBinding(
        val scheme: Scheme) : Binding()

data class TypeBinding(
        val type: Type) : Binding()


data class Environment(val state: Map<String, Binding> = mapOf()) {
    fun value(name: String): Scheme? {
        val binding =
                state[name]

        return if (binding == null) {
            null
        } else {
            when (binding) {
                is ValueBinding ->
                    binding.scheme

                else ->
                    null
            }
        }
    }

    fun newValue(name: String, scheme: Scheme): Environment =
            Environment(this.state + Pair(name, ValueBinding(scheme)))

    fun newType(name: String, type: Type): Environment =
            Environment(this.state + Pair(name, TypeBinding(type)))

    fun containsKey(name: String): Boolean =
            state.containsKey(name)
}


val emptyEnvironment =
        Environment()