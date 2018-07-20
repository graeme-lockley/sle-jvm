package za.co.no9.sle.inference

import za.co.no9.sle.Location
import za.co.no9.sle.typing.Schema
import za.co.no9.sle.typing.Type


sealed class Node(
        open val location: Location)


data class Module(
        override val location: Location,
        val declarations: List<Declaration>) : Node(location)


sealed class Declaration(
        location: Location) : Node(location)

data class LetDeclaration(
        override val location: Location,
        val type: Type,
        val name: ID,
        val expression: Expression) : Declaration(location)

data class TypeAliasDeclaration(
        override val location: Location,
        val name: ID,
        val schema: Schema) : Declaration(location)


data class ID(
        override val location: Location,
        val name: String) : Node(location)


sealed class Expression(
        location: Location,
        open val type: Type) : Node(location)

data class ConstantBool(
        override val location: Location,
        override val type: Type,
        val value: Boolean) : Expression(location, type)

data class ConstantInt(
        override val location: Location,
        override val type: Type,
        val value: Int) : Expression(location, type)

data class ConstantString(
        override val location: Location,
        override val type: Type,
        val value: String) : Expression(location, type)

data class IdReference(
        override val location: Location,
        override val type: Type,
        val name: String) : Expression(location, type)

data class IfExpression(
        override val location: Location,
        override val type: Type,
        val guardExpression: Expression,
        val thenExpression: Expression,
        val elseExpression: Expression) : Expression(location, type)

data class LambdaExpression(
        override val location: Location,
        override val type: Type,
        val argument: ID,
        val expression: Expression) : Expression(location, type)

data class CallExpression(
        override val location: Location,
        override val type: Type,
        val operator: Expression,
        val operand: Expression) : Expression(location, type)