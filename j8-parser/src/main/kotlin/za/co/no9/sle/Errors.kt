package za.co.no9.sle


data class Position(
        private val line: Int,
        private val column: Int) {
    override fun toString(): String =
            "($line, $column)"
}

data class Location(
        private val start: Position,
        private val end: Position) {
    constructor(position: Position) : this(position, position)

    override fun toString(): String =
            "[$start $end]"
}


typealias Errors =
        List<Error>


sealed class Error

sealed class LocationError(
        open val location: Location) : Error()

data class SyntaxError(
        override val location: Location,
        val msg: String) : LocationError(location)

data class UnboundVariable(
        override val location: Location,
        val name: String) : LocationError(location)

data class DuplicateLetDeclaration(
        override val location: Location,
        val name: String) : LocationError(location)

data class UnificationFail(
        val t1: Type,
        val t2: Type) : Error()

data class UnificationMismatch(
        val t1s: List<Type>,
        val t2s: List<Type>) : Error()