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


sealed class Node(
        open val location: Location)


sealed class Expression(
        location: Location) : Node(location)


data class True(
        override val location: Location) : Expression(location)

data class False(
        override val location: Location) : Expression(location)

data class ConstantInt(
        override val location: Location,
        val value: Int) : Expression(location)

data class ConstantString(
        override val location: Location,
        val value: String) : Expression(location)

data class NotExpression(
        override val location: Location,
        val expression: Expression) : Expression(location)

data class IdReference(
        override val location: Location,
        val name: String) : Expression(location)

data class IfExpression(
        override val location: Location,
        val guardExpression: Expression,
        val thenExpression: Expression,
        val elseExpression: Expression) : Expression(location)

data class LambdaExpression(
        override val location: Location,
        val arguments: List<IdReference>,
        val expression: Expression) : Expression(location)

data class BinaryOpExpression(
        override val location: Location,
        val left: Expression,
        val operator: IdReference,
        val right: Expression) : Expression(location)

data class CallExpression(
        override val location: Location,
        val operator: Expression,
        val operands: List<Expression>) : Expression(location)