package za.co.no9.sle.typing

import za.co.no9.sle.QString


data class Environment(private val valueBindings: Map<String, ValueBinding> = mapOf(), private val typeBindings: Map<String, TypeBinding> = mapOf()) {
    fun value(name: String): ValueBinding? =
            valueBindings[name]

    fun value(name: QString): ValueBinding? =
            if (name.qualifier == null)
                valueBindings[name.string]
            else
                import(name.qualifier)?.value(name.string)


    fun variable(name: String): Scheme? {
        val valueBinding =
                value(name)

        return when (valueBinding) {
            is VariableBinding ->
                valueBinding.scheme

            else ->
                null
        }
    }

    fun variable(name: QString): Scheme? =
            if (name.qualifier == null)
                variable(name.string)
            else
                import(name.qualifier)?.variable(name.string)


    fun import(name: String): Environment? {
        val valueBinding =
                value(name)

        return when (valueBinding) {
            is ImportBinding ->
                valueBinding.environment

            else ->
                null
        }
    }

    fun type(name: String): TypeBinding? =
            typeBindings[name]

    fun type(name: QString): TypeBinding? =
            if (name.qualifier == null)
                type(name.string)
            else {
                val valueBinding =
                        value(name.qualifier)

                when (valueBinding) {
                    is ImportBinding ->
                        valueBinding.environment.type(name.string)

                    else ->
                        null
                }
            }

    fun newValue(name: String, value: ValueBinding): Environment =
            Environment(this.valueBindings + Pair(name, value), this.typeBindings)

    fun newType(name: String, binding: TypeBinding): Environment =
            Environment(this.valueBindings, this.typeBindings + Pair(name, binding))

    fun containsValue(name: String): Boolean =
            valueBindings.contains(name)

    fun containsType(name: String): Boolean =
            type(name) != null

    fun containsType(name: QString): Boolean =
            type(name) != null
}


sealed class ValueBinding

data class VariableBinding(
        val scheme: Scheme) : ValueBinding()

data class ImportVariableBinding(
        val scheme: Scheme) : ValueBinding()

data class ImportBinding(
        val environment: Environment) : ValueBinding()


sealed class TypeBinding(
        open val scheme: Scheme)

data class BuiltinBinding(
        override val scheme: Scheme) : TypeBinding(scheme)

data class AliasBinding(
        override val scheme: Scheme) : TypeBinding(scheme)

data class ImportAliasBinding(
        override val scheme: Scheme) : TypeBinding(scheme)

data class ADTBinding(
        override val scheme: Scheme,
        val constructors: List<Pair<String, Scheme>>) : TypeBinding(scheme)

data class ImportADTBinding(
        override val scheme: Scheme,
        val constructors: List<Pair<String, Scheme>>) : TypeBinding(scheme)

data class OpaqueImportADTBinding(
        override val scheme: Scheme) : TypeBinding(scheme)


val emptyEnvironment =
        Environment()

val initialEnvironment =
        emptyEnvironment
                .newType("Int", BuiltinBinding(Scheme(listOf(), typeInt)))
                .newType("Bool", BuiltinBinding(Scheme(listOf(), typeBool)))
                .newType("String", BuiltinBinding(Scheme(listOf(), typeString)))
                .newType("()", BuiltinBinding(Scheme(listOf(), typeUnit)))
