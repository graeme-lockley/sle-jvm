package za.co.no9.sle.typing


data class Environment(val valueBindings: Map<String, Scheme> = mapOf(), val typeBindings: Map<String, Scheme> = mapOf()) {
    fun value(name: String): Scheme? =
            valueBindings[name]

    fun newValue(name: String, scheme: Scheme): Environment =
            Environment(this.valueBindings + Pair(name, scheme), this.typeBindings)

    fun newType(name: String, scheme: Scheme): Environment =
            Environment(this.valueBindings, this.typeBindings + Pair(name, scheme))

    fun containsValue(name: String): Boolean =
            valueBindings.contains(name)

    fun containsType(name: String): Boolean =
            typeBindings.contains(name)
}


val emptyEnvironment =
        Environment()