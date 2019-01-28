package za.co.no9.sle.typing

import za.co.no9.sle.QString
import za.co.no9.sle.repository.Item


sealed class Associativity {
    abstract fun asString(): String
}

object Left : Associativity() {
    override fun asString() =
            "left"
}

object Right : Associativity() {
    override fun asString() =
            "right"
}

object None : Associativity() {
    override fun asString() =
            "none"
}


fun associativityFromString(text: String): Associativity =
        when (text) {
            "left" ->
                Left

            "right" ->
                Right

            else ->
                None
        }


data class Environment(val valueBindings: Map<String, ValueBinding> = mapOf(), val typeBindings: Map<String, TypeBinding> = mapOf()) {
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

            is OperatorBinding ->
                valueBinding.scheme

            is ImportVariableBinding ->
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


    fun alias(name: QString): Scheme? {
        val typeBinding =
                type(name)

        return when (typeBinding) {
            is AliasBinding ->
                typeBinding.scheme

            is ImportAliasBinding ->
                typeBinding.scheme

            else ->
                null
        }
    }


    fun alias(name: String): Scheme? =
            alias(QString(name))


    fun newValue(name: String, value: ValueBinding): Environment =
            Environment(this.valueBindings + Pair(name, value), this.typeBindings)


    fun newType(name: String, binding: TypeBinding): Environment =
            Environment(this.valueBindings, this.typeBindings + Pair(name, binding))


    fun containsValue(name: String): Boolean =
            valueBindings.contains(name)


    fun containsType(name: String): Boolean =
            type(name) != null
}


sealed class ValueBinding

data class VariableBinding(
        val scheme: Scheme) : ValueBinding()

data class OperatorBinding(
        val scheme: Scheme,
        val precedence: Int,
        val associativity: Associativity) : ValueBinding()

data class ImportVariableBinding(
        val item: Item,
        val scheme: Scheme) : ValueBinding()

data class ImportOperatorBinding(
        val item: Item,
        val scheme: Scheme,
        val precedence: Int,
        val associativity: Associativity) : ValueBinding()

data class ImportBinding(
        val environment: Environment) : ValueBinding()


sealed class TypeBinding {
    abstract fun numberOfParameters(): Int
}

data class BuiltinBinding(
        val scheme: Scheme) : TypeBinding() {
    override fun numberOfParameters(): Int =
            scheme.parameters.size
}

data class AliasBinding(
        val scheme: Scheme) : TypeBinding() {
    override fun numberOfParameters(): Int =
            scheme.parameters.size
}

data class ImportAliasBinding(
        val item: Item,
        val scheme: Scheme) : TypeBinding() {
    override fun numberOfParameters(): Int =
            scheme.parameters.size
}

data class ADTBinding(
        val scheme: Scheme,
        val constructors: List<Pair<String, Scheme>>) : TypeBinding() {
    override fun numberOfParameters(): Int =
            scheme.parameters.size
}

data class ImportADTBinding(
        val item: Item,
        val cardinality: Int,
        val identity: String,
        val constructors: List<Pair<String, Scheme>>) : TypeBinding() {
    override fun numberOfParameters(): Int =
            cardinality
}

data class OpaqueImportADTBinding(
        val item: Item,
        val cardinality: Int,
        val identity: String) : TypeBinding() {
    override fun numberOfParameters(): Int =
            cardinality
}


fun resolveAlias(env: Environment, type: Type?): Type? =
        when (type) {
            null ->
                null

            is TAlias -> {
                val alias =
                        env.alias(type.name)

                when (alias) {
                    null ->
                        null

                    else -> {
                        val substitutionMap =
                                alias.parameters.zip(type.arguments).fold(emptyMap<Var, Type>()) { a, b -> a + b }

                        alias.type.apply(Substitution(substitutionMap))
                    }
                }
            }
            else ->
                type
        }


val emptyEnvironment =
        Environment()

val initialEnvironment =
        emptyEnvironment
                .newType("Int", BuiltinBinding(Scheme(listOf(), typeInt)))
                .newType("Bool", BuiltinBinding(Scheme(listOf(), typeBool)))
                .newType("String", BuiltinBinding(Scheme(listOf(), typeString)))
                .newType("Char", BuiltinBinding(Scheme(listOf(), typeChar)))
                .newType("()", BuiltinBinding(Scheme(listOf(), typeUnit)))
