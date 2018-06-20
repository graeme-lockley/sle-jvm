package za.co.no9.sle.ast


data class Position(private val line: Int, private val column: Int) {
    override fun toString(): String =
            "($line, $column)"
}


sealed class Node(open val position: Position)


sealed class Expression(position: Position) : Node(position)


data class True(override val position: Position) : Expression(position)

data class False(override val position: Position) : Expression(position)

data class ConstantInt(override val position: Position, val value: Int): Expression(position)

data class ConstantString(override val position: Position, val value: String): Expression(position)

data class NotExpression(override val position: Position, val expression: Expression): Expression(position)

data class IdRefernce(override val position: Position, val id: String): Expression(position)

