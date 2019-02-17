package za.co.no9.sle.repository

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import za.co.no9.sle.Location
import za.co.no9.sle.typing.TArr
import za.co.no9.sle.typing.TCon
import za.co.no9.sle.typing.TRec
import za.co.no9.sle.typing.TVar

private val gson =
        GsonBuilder().setPrettyPrinting().create()


fun toJsonString(export: Export): String =
        gson.toJson(export)


fun fromJsonString(input: String): Export {
    fun jsonToType(type: JsonObject): Type =
            when {
                type.has("variable") ->
                    Variable(type["variable"].asInt)

                type.has("constant") -> {
                    val constant =
                            type["constant"].asString

                    Constant(constant, type["arguments"].asJsonArray.map { jsonToType(it.asJsonObject) })
                }

                type.has("domain") ->
                    Arrow(jsonToType(type["domain"].asJsonObject), jsonToType(type["range"].asJsonObject))

                else ->
                    Record(type["fixed"].asBoolean, type["fields"].asJsonArray.map {
                        val field =
                                it.asJsonObject

                        Field(field["name"].asString, jsonToType(field["type"].asJsonObject))
                    })
            }

    fun jsonToScheme(scheme: JsonObject): Scheme =
            Scheme(scheme["parameters"].asJsonArray.map { it.asInt }, jsonToType(scheme["type"].asJsonObject))


    fun jsonToConstructor(constructor: JsonObject): Constructor =
            Constructor(constructor["name"].asString, jsonToScheme(constructor["scheme"].asJsonObject))


    val declarations =
            JsonParser().parse(input).asJsonObject["declarations"].asJsonArray

    return Export(declarations.map { d ->
        val declaration =
                d.asJsonObject

        when {
            declaration.has("name") ->
                LetDeclaration(declaration["name"].asString, jsonToScheme(declaration["scheme"].asJsonObject))

            declaration.has("operator") ->
                OperatorDeclaration(declaration["operator"].asString, jsonToScheme(declaration["scheme"].asJsonObject), declaration["precedence"].asInt, declaration["associativity"].asString)

            declaration.has("alias") ->
                AliasDeclaration(declaration["alias"].asString, jsonToScheme(declaration["scheme"].asJsonObject))

            declaration.has("constructors") ->
                ADTDeclaration(declaration["adt"].asString, declaration["cardinality"].asInt, declaration["identity"].asString, declaration["constructors"].asJsonArray.map { jsonToConstructor(it.asJsonObject) })

            else ->
                OpaqueADTDeclaration(declaration["adt"].asString, declaration["cardinality"].asInt, declaration["identity"].asString)
        }
    })
}


data class Export(
        val declarations: List<Declaration>) {

    operator fun get(name: String): Declaration? =
            declarations.firstOrNull {
                when (it) {
                    is LetDeclaration ->
                        it.name == name

                    is OperatorDeclaration ->
                        it.operator == name

                    is AliasDeclaration ->
                        it.alias == name

                    is OpaqueADTDeclaration ->
                        it.adt == name

                    is ADTDeclaration ->
                        it.adt == name
                }
            }
}

sealed class Declaration


data class LetDeclaration(
        val name: String,
        val scheme: Scheme) : Declaration()

data class OperatorDeclaration(
        val operator: String,
        val scheme: Scheme,
        val precedence: Int,
        val associativity: String) : Declaration()

data class AliasDeclaration(
        val alias: String,
        val scheme: Scheme) : Declaration()

data class OpaqueADTDeclaration(
        val adt: String,
        val cardinality: Int,
        val identity: String) : Declaration()

data class ADTDeclaration(
        val adt: String,
        val cardinality: Int,
        val identity: String,
        val constructors: List<Constructor>) : Declaration()

data class Constructor(
        val name: String,
        val scheme: Scheme)


data class Scheme(val parameters: List<Int>, val type: Type) {
    fun asScheme(location: Location): za.co.no9.sle.typing.Scheme =
            za.co.no9.sle.typing.Scheme(parameters, type.asType(location))
}

sealed class Type {
    abstract fun asType(location: Location): za.co.no9.sle.typing.Type
}

data class Variable(val variable: Int) : Type() {
    override fun asType(location: Location): za.co.no9.sle.typing.Type =
            TVar(location, variable)
}

data class Constant(val constant: String, val arguments: List<Type>) : Type() {
    override fun asType(location: Location): za.co.no9.sle.typing.Type =
            TCon(location, constant, arguments.map { it.asType(location) })
}

data class Arrow(val domain: Type, val range: Type) : Type() {
    override fun asType(location: Location): za.co.no9.sle.typing.Type =
            TArr(location, domain.asType(location), range.asType(location))
}

data class Record(val fixed: Boolean, val fields: List<Field>) : Type() {
    override fun asType(location: Location): za.co.no9.sle.typing.Type =
            TRec(location, fixed, fields.map { Pair(it.name, it.type.asType(location)) })
}

data class Field(val name: String, val type: Type)