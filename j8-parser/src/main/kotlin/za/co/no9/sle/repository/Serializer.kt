package za.co.no9.sle.repository

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import za.co.no9.sle.Location
import za.co.no9.sle.QString
import za.co.no9.sle.typing.TArr
import za.co.no9.sle.typing.TCon
import za.co.no9.sle.typing.TVar

private val gson =
        GsonBuilder().setPrettyPrinting().create()


fun toJsonString(export: Export): String =
        gson.toJson(export)


fun fromJsonString(input: String): Export {
    val declarations =
            JsonParser().parse(input).asJsonObject["declarations"].asJsonArray

    return Export(declarations.map { d ->
        val declaration =
                d.asJsonObject

        when {
            declaration.has("name") ->
                LetDeclaration(declaration["name"].asString, jsonToScheme(declaration["scheme"].asJsonObject))

            declaration.has("alias") ->
                AliasDeclaration(declaration["alias"].asString, jsonToScheme(declaration["scheme"].asJsonObject))

            declaration.has("constructors") ->
                FullADTDeclaration(declaration["adt"].asString, jsonToScheme(declaration["scheme"].asJsonObject), declaration["constructors"].asJsonArray.map { jsonToConstructor(it.asJsonObject) })

            else ->
                ADTDeclaration(declaration["adt"].asString, jsonToScheme(declaration["scheme"].asJsonObject))
        }
    })
}


private fun jsonToConstructor(constructor: JsonObject): Constructor =
        Constructor(constructor["name"].asString, jsonToScheme(constructor["scheme"].asJsonObject))


private fun jsonToScheme(scheme: JsonObject): Scheme =
        Scheme(scheme["parameters"].asJsonArray.map { it.asInt }, jsonToType(scheme["type"].asJsonObject))


private fun jsonToType(type: JsonObject): Type =
        when {
            type.has("variable") ->
                Variable(type["variable"].asInt)

            type.has("constant") ->
                Constant(QString(type["constant"].asString), type["arguments"].asJsonArray.map { jsonToType(it.asJsonObject) })

            else ->
                Arrow(jsonToType(type["domain"].asJsonObject), jsonToType(type["range"].asJsonObject))
        }


data class Export(
        val declarations: List<Declaration>) {

    operator fun get(name: String): Declaration? =
            declarations.firstOrNull {
                when (it) {
                    is LetDeclaration ->
                        it.name == name

                    is AliasDeclaration ->
                        it.alias == name

                    is ADTDeclaration ->
                        it.adt == name

                    is FullADTDeclaration ->
                        it.adt == name
                }
            }
}

sealed class Declaration


data class LetDeclaration(
        val name: String,
        val scheme: Scheme) : Declaration()

data class AliasDeclaration(
        val alias: String,
        val scheme: Scheme) : Declaration()

data class ADTDeclaration(
        val adt: String,
        val scheme: Scheme) : Declaration()

data class FullADTDeclaration(
        val adt: String,
        val scheme: Scheme,
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

data class Constant(val constant: QString, val arguments: List<Type>) : Type() {
    override fun asType(location: Location): za.co.no9.sle.typing.Type =
            TCon(location, constant, arguments.map { it.asType(location) })
}

data class Arrow(val domain: Type, val range: Type) : Type() {
    override fun asType(location: Location): za.co.no9.sle.typing.Type =
            TArr(domain.asType(location), range.asType(location))
}
