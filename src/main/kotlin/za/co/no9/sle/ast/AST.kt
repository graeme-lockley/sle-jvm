package za.co.no9.sle.ast


data class Position(
        private val line: Int,
        private val column: Int) {
    override fun toString(): String =
            "($line, $column)"
}

data class Location(
        private val start: Position,
        private val end: Position) {
    override fun toString(): String =
            "[$start $end]"
}


