package za.co.no9.sle.ast.pass1

import za.co.no9.sle.Location


sealed class Node(
        open val location: Location)


data class Module(
        override val location: Location,
        val declarations: List<Declaration>) : Node(location)


sealed class Declaration(
        location: Location) : Node(location)

data class LetDeclaration(
        override val location: Location,
        val name: ID,
        val arguments: List<ID>,
        val expression: Expression) : Declaration(location)


data class ID(
        override val location: Location,
        val name: String) : Node(location)


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
        val arguments: List<ID>,
        val expression: Expression) : Expression(location)

data class BinaryOpExpression(
        override val location: Location,
        val left: Expression,
        val operator: ID,
        val right: Expression) : Expression(location)

data class CallExpression(
        override val location: Location,
        val operator: Expression,
        val operands: List<Expression>) : Expression(location)