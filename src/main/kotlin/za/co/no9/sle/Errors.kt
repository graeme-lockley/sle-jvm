package za.co.no9.sle


data class Position(val line: Int, val column: Int)

data class SyntaxError(val position: Position, val msg: String)