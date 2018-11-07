package za.co.no9.sle.ast.typeless

import za.co.no9.sle.Location


sealed class Node(
        open val location: Location) {
    val column: Int
        get() = this.location.start.column
}


data class Module(
        override val location: Location,
        val exports: List<Export>,
        val declarations: List<Declaration>) : Node(location)


sealed class Export(
        location: Location) : Node(location)

data class LetExport(
        override val location: Location,
        val name: ID) : Export(location)

data class TypeExport(
        override val location: Location,
        val name: ID,
        val withConstructors: Boolean) : Export(location)


sealed class Declaration(
        location: Location,
        open val name: ID) : Node(location)

data class LetSignature(
        override val location: Location,
        override val name: ID,
        val type: TType) : Declaration(location, name)

data class LetDeclaration(
        override val location: Location,
        override val name: ID,
        val arguments: List<Pattern>,
        val expression: Expression) : Declaration(location, name)

data class LetGuardDeclaration(
        override val location: Location,
        override val name: ID,
        val arguments: List<Pattern>,
        val guardedExpressions: List<Pair<Expression, Expression>>) : Declaration(location, name)

data class TypeAliasDeclaration(
        override val location: Location,
        override val name: ID,
        val type: TType) : Declaration(location, name)

data class TypeDeclaration(
        override val location: Location,
        override val name: ID,
        val arguments: List<ID>,
        val constructors: List<TypeConstructor>) : Declaration(location, name)

data class TypeConstructor(
        override val location: Location,
        val name: ID,
        val arguments: List<TType>) : Node(location)


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

data class ConstructorReference(
        override val location: Location,
        val name: String) : Expression(location)

data class IfExpression(
        override val location: Location,
        val guardExpression: Expression,
        val thenExpression: Expression,
        val elseExpression: Expression) : Expression(location)

data class LambdaExpression(
        override val location: Location,
        val arguments: List<Pattern>,
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

data class CaseExpression(
        override val location: Location,
        val operator: Expression,
        val items: List<CaseItem>) : Expression(location)

data class CaseItem(
        override val location: Location,
        val pattern: Pattern,
        val expression: Expression) : Node(location)


sealed class Pattern(
        override val location: Location) : Node(location)

data class ConstantIntPattern(
        override val location: Location,
        val value: Int) : Pattern(location)

data class ConstantBoolPattern(
        override val location: Location,
        val value: Boolean) : Pattern(location)

data class ConstantStringPattern(
        override val location: Location,
        val value: String) : Pattern(location)

data class ConstantUnitPattern(
        override val location: Location) : Pattern(location)

data class IdReferencePattern(
        override val location: Location,
        val name: String) : Pattern(location)

data class ConstructorReferencePattern(
        override val location: Location,
        val name: String,
        val parameters: List<Pattern>) : Pattern(location)


sealed class TType(
        override val location: Location) : Node(location)

data class TUnit(
        override val location: Location) : TType(location)

data class TVarReference(
        override val location: Location,
        val name: String) : TType(location)

data class TConstReference(
        override val location: Location,
        val name: ID,
        val arguments: List<TType>) : TType(location)

data class TArrow(
        override val location: Location,
        val domain: TType,
        val range: TType) : TType(location)
