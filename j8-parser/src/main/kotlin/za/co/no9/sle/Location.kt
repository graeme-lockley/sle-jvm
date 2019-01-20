package za.co.no9.sle


data class Location(
        val start: Position,
        val end: Position) {
    constructor(position: Position) : this(position, position)

    override fun toString(): String =
            "[$start $end]"


    operator fun plus(location: Location?): Location =
            if (location == null)
                this
            else
                Location(
                        if (start > location.start) location.start else start,
                        if (end < location.end) location.end else end)
}


val homeLocation =
        Location(Position(0, 0))


fun location(nodes: List<Location>): Location? =
        if (nodes.isEmpty())
            null
        else
            nodes.fold(nodes[0]) { a, b -> a + b }

