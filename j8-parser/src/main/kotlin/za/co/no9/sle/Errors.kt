package za.co.no9.sle

import za.co.no9.sle.typing.Type


data class Position(
        val line: Int,
        val column: Int) {
    override fun toString(): String =
            "($line, $column)"

    operator fun compareTo(other: Position): Int =
            when {
                line < other.line ->
                    -1

                line > other.line ->
                    1

                column < other.column ->
                    -1

                column > other.column ->
                    1

                else ->
                    0
            }
}

data class Location(
        val start: Position,
        val end: Position) {
    constructor(position: Position) : this(position, position)

    override fun toString(): String =
            "[$start $end]"


    operator fun plus(location: Location?): Location =
            if (location == null) this else
                Location(
                        if (start > location.start) location.start else start,
                        if (end < location.end) location.end else end)
}


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

data class UnificationFail(
        val t1: Type,
        val t2: Type) : Error()

data class UnificationMismatch(
        val t1s: List<Type>,
        val t2s: List<Type>) : Error()

data class UnknownType(
        val name: String) : Error()

