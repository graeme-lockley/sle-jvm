package za.co.no9.sle

import za.co.no9.sle.typing.Scheme
import za.co.no9.sle.typing.TRec
import za.co.no9.sle.typing.Type
import java.io.File


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
        val name: QString) : LocationError(location)

data class UnknownTypeReference(
        override val location: Location,
        val name: QString) : LocationError(location)

data class UnknownConstructorReference(
        override val location: Location,
        val name: QString) : LocationError(location)

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
        val name: QString,
        val expected: Int,
        val actual: Int) : LocationError(location)

data class IncorrectNumberOfAliasArguments(
        override val location: Location,
        val name: QString,
        val expected: Int,
        val actual: Int) : LocationError(location)

data class IncorrectNumberOfConstructorArguments(
        override val location: Location,
        val name: QString,
        val expected: Int,
        val actual: Int) : LocationError(location)

data class TooManyTupleArguments(
        override val location: Location,
        val maximum: Int,
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
        val t1s: Collection<Type>,
        val t2s: Collection<Type>) : Error()

data class DifferingRecordSize (
        val t1: TRec,
        val t2: TRec): Error()

data class RecordFieldNamesMismatch (
        val t1: TRec,
        val t2: TRec): Error()



data class UnknownType(
        override val location: Location,
        val name: QString) : LocationError(location)

data class NonExhaustivePattern(
        override val location: Location) : LocationError(location)

data class ValueNotExported(
        override val location: Location,
        val name: String) : LocationError(location)

data class TypeNotExported(
        override val location: Location,
        val name: String) : LocationError(location)

data class TypeAliasHasNoConstructors(
        override val location: Location,
        val name: String) : LocationError(location)

data class ADTHasNoConstructors(
        override val location: Location,
        val name: String) : LocationError(location)

data class TypeConstructorNotExported(
        override val location: Location,
        val name: String) : LocationError(location)

data class DuplicateImportedTypeDeclaration(
        override val location: Location,
        val name: String) : LocationError(location)

data class DuplicateImportedTypeAliasDeclaration(
        override val location: Location,
        val name: String) : LocationError(location)

data class DuplicateImportedLetDeclaration(
        override val location: Location,
        val name: String) : LocationError(location)

data class DuplicateImportedName(
        override val location: Location,
        val name: String) : LocationError(location)




data class UnableToReadFile(
        val file: File) : Error()

data class IOException(
        val e: java.io.IOException) : Error()

data class CyclicDependency(
        val file: URN) : Error()

data class ImportErrors(
        override val location: Location,
        val urn: URN,
        val errors: Errors) : LocationError(location)

