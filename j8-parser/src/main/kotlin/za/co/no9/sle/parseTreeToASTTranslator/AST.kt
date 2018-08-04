package za.co.no9.sle.parseTreeToASTTranslator

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
        val schema: TSchema?,
        val expression: Expression) : Declaration(location)


data class TypeAliasDeclaration(
        override val location: Location,
        val name: ID,
        val schema: TSchema) : Declaration(location)


data class ID(
        override val location: Location,
        val name: String) : Node(location)


sealed class Expression(
        location: Location) : Node(location)

data class Unit(
        override val location: Location) : Expression(location)

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


data class TSchema(
        val location: Location,
        val parameters: List<TypeParameter>,
        val type: TType)

data class TypeParameter(
        val location: Location,
        val name: ID,
        val type: TType?)

sealed class TType(
        open val location: Location)

data class TUnit(
        override val location: Location) : TType(location)

data class TIdReference(
        override val location: Location,
        val name: String) : TType(location)

data class TArrow(
        override val location: Location,
        val domain: TType,
        val range: TType) : TType(location)
