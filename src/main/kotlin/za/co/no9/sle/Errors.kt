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


sealed class Error(
        open val location: Location)

data class SyntaxError(
        override val location: Location,
        val msg: String) : Error(location)

data class UnboundVariable(
        override val location: Location,
        val name: String) : Error(location)
