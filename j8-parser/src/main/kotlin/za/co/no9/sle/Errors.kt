package za.co.no9.sle

import za.co.no9.sle.typing.Scheme
import za.co.no9.sle.typing.Type


typealias Errors =
        Set<Error>


sealed class Error

sealed class LocationError(
        open val location: Location) : Error()

data class SyntaxError(
        override val location: Location,
        val msg: String) : LocationError(location)

data class UnboundVariable(
        override val location: Location,
        val name: String) : LocationError(location)

data class UnknownTypeReference(
        override val location: Location,
        val name: String) : LocationError(location)

data class UnknownConstructorReference(
        override val location: Location,
        val name: String) : LocationError(location)

data class DuplicateTypeDeclaration(
        override val location: Location,
        val name: String) : LocationError(location)

data class DuplicateTypeAliasDeclaration(
        override val location: Location,
        val name: String) : LocationError(location)

data class DuplicateLetDeclaration(
        override val location: Location,
        val name: String) : LocationError(location)

data class DuplicateConstructorDeclaration(
        override val location: Location,
        val name: String) : LocationError(location)

data class DuplicateLetSignature(
        override val location: Location,
        val otherLocation: Location,
        val name: String) : LocationError(location)

data class LetSignatureWithoutDeclaration(
        override val location: Location,
        val name: String) : LocationError(location)

data class IncorrectNumberOfSchemeArguments(
        override val location: Location,
        val name: String,
        val expected: Int,
        val actual: Int) : LocationError(location)

data class IncorrectNumberOfAliasArguments(
        override val location: Location,
        val name: String,
        val expected: Int,
        val actual: Int) : LocationError(location)

data class IncorrectNumberOfConstructorArguments(
        override val location: Location,
        val name: String,
        val expected: Int,
        val actual: Int) : LocationError(location)

data class IncompatibleDeclarationSignature(
        override val location: Location,
        val name: String,
        val expected: Scheme,
        val inferred: Scheme) : LocationError(location)

data class UnificationFail(
        val t1: Type,
        val t2: Type) : Error()

data class UnificationMismatch(
        val t1s: List<Type>,
        val t2s: List<Type>) : Error()

data class UnknownType(
        override val location: Location,
        val name: String) : LocationError(location)

data class NonExhaustivePattern(
        override val location: Location) : LocationError(location)


data class WriteFileError(
        val e: Exception): Error()

