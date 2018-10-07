package za.co.no9.sle

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