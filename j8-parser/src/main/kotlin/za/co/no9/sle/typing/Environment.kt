package za.co.no9.sle.typing


data class Environment(val valueBindings: Map<String, Scheme> = mapOf(), val typeBindings: Map<String, TypeBinding> = mapOf()) {
    fun value(name: String): Scheme? =
            valueBindings[name]

    fun type(name: String): TypeBinding? =
            typeBindings[name]

    fun newValue(name: String, scheme: Scheme): Environment =
            Environment(this.valueBindings + Pair(name, scheme), this.typeBindings)

    fun newType(name: String, binding: TypeBinding): Environment =
            Environment(this.valueBindings, this.typeBindings + Pair(name, binding))

    fun containsValue(name: String): Boolean =
            valueBindings.contains(name)

    fun containsType(name: String): Boolean =
            typeBindings.contains(name)
}


sealed class TypeBinding(open val scheme: Scheme)

data class BuiltinBinding(override val scheme: Scheme) : TypeBinding(scheme)

data class AliasBinding(override val scheme: Scheme) : TypeBinding(scheme)

data class ADTBinding(override val scheme: Scheme) : TypeBinding(scheme)


val emptyEnvironment =
        Environment()

val initialEnvironment =
        emptyEnvironment
                .newType("Int", BuiltinBinding(Scheme(listOf(), typeInt)))
                .newType("Bool", BuiltinBinding(Scheme(listOf(), typeBool)))
                .newType("String", BuiltinBinding(Scheme(listOf(), typeString)))
                .newType("()", BuiltinBinding(Scheme(listOf(), typeUnit)))
