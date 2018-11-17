package za.co.no9.sle.repository

import com.google.gson.GsonBuilder

private val gson =
        GsonBuilder().setPrettyPrinting().create()


fun toJsonString(export: Export): String =
        gson.toJson(export)


fun fromJsonString(input: String): Export =
        gson.fromJson(input, Export::class.java)


data class Export(
        val declarations: List<Declaration>)

sealed class Declaration


data class LetDeclaration(
        val name: String,
        val scheme: Scheme) : Declaration()

data class AliasDeclaration(
        val alias: String,
        val scheme: Scheme) : Declaration()

data class ADTDeclaration(
        val adt: String,
        val scheme: Scheme,
        val constructors: List<Constructor>) : Declaration()

data class Constructor(
        val name: String,
        val scheme: Scheme)


data class Scheme(val parameters: List<Int>, val type: Type)

sealed class Type

data class Variable(val variable: Int) : Type()

data class Constant(val constant: String, val arguments: List<Type>) : Type()

data class Arrow(val domain: Type, val range: Type) : Type()
