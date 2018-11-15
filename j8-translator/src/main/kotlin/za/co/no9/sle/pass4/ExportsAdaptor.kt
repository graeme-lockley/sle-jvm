package za.co.no9.sle.pass4

import com.google.gson.GsonBuilder
import za.co.no9.sle.ast.core.*

private val gson =
        GsonBuilder().setPrettyPrinting().create()


fun toJsonString(nameDeclarations: List<NameDeclaration>): String =
        gson.toJson(toClass(nameDeclarations))


fun fromJsonString(input: String): List<NameDeclaration> =
        emptyList()


private fun toClass(nameDeclarations: List<NameDeclaration>): Export =
        Export(nameDeclarations.map { toClass(it) })


private fun toClass(nameDeclaration: NameDeclaration): Declaration =
        when (nameDeclaration) {
            is ValueNameDeclaration ->
                LetDeclaration(nameDeclaration.name, toClass(nameDeclaration.scheme))

            is AliasNameDeclaration ->
                AliasDeclaration(nameDeclaration.name, toClass(nameDeclaration.scheme))

            is ADTNameDeclaration ->
                ADTDeclaration(nameDeclaration.name, toClass(nameDeclaration.scheme), emptyList())

            is FullADTNameDeclaration ->
                ADTDeclaration(nameDeclaration.name, toClass(nameDeclaration.scheme), nameDeclaration.constructors.map { Constructor(it.name, toClass(it.scheme)) })
        }


private fun toClass(scheme: za.co.no9.sle.typing.Scheme): Scheme =
        Scheme(scheme.parameters, toClass(scheme.type))

private fun toClass(type: za.co.no9.sle.typing.Type): Type =
        when (type) {
            is za.co.no9.sle.typing.TVar ->
                Variable(type.variable)

            is za.co.no9.sle.typing.TCon ->
                Constant(type.name, type.arguments.map { toClass(it) })

            is za.co.no9.sle.typing.TArr ->
                Arrow(toClass(type.domain), toClass(type.range))
        }

private data class Export(
        val declarations: List<Declaration>)

private sealed class Declaration

private data class LetDeclaration(
        val name: String,
        val scheme: Scheme) : Declaration()

private data class AliasDeclaration(
        val alias: String,
        val scheme: Scheme) : Declaration()

private data class ADTDeclaration(
        val adt: String,
        val scheme: Scheme,
        val constructors: List<Constructor>) : Declaration()

private data class Constructor(
        val name: String,
        val scheme: Scheme)


private data class Scheme(val parameters: List<Int>, val type: Type)

private sealed class Type

private data class Variable(val variable: Int) : Type()

private data class Constant(val constant: String, val arguments: List<Type>) : Type()

private data class Arrow(val domain: Type, val range: Type) : Type()
