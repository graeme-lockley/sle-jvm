package za.co.no9.sle.ast.typedPattern

import za.co.no9.sle.Location
import za.co.no9.sle.typing.Associativity
import za.co.no9.sle.typing.Scheme
import za.co.no9.sle.typing.Type


sealed class Node(
        open val location: Location)


data class Module(
        override val location: Location,
        val exports: List<ExportDeclaration>,
        val declarations: List<Declaration>) : Node(location)


sealed class ExportDeclaration(
        open val name: String)

data class ValueExportDeclaration(
        override val name: String,
        val scheme: Scheme) : ExportDeclaration(name)

data class OperatorExportDeclaration(
        override val name: String,
        val scheme: Scheme,
        val precedence: Int,
        val associativity: Associativity) : ExportDeclaration(name)

data class AliasExportDeclaration(
        override val name: String,
        val scheme: Scheme) : ExportDeclaration(name)

data class ADTExportDeclaration(
        override val name: String,
        val scheme: Scheme) : ExportDeclaration(name)

data class FullADTExportDeclaration(
        override val name: String,
        val scheme: Scheme,
        val constructors: List<ConstructorNameDeclaration>) : ExportDeclaration(name)

data class ConstructorNameDeclaration(
        val name: String,
        val scheme: Scheme)


sealed class Declaration(
        location: Location) : Node(location)

data class LetDeclaration(
        override val location: Location,
        val scheme: Scheme,
        val id: ValueDeclarationID,
        val expressions: List<Expression>) : Declaration(location)

data class TypeAliasDeclaration(
        override val location: Location,
        val name: ID,
        val scheme: Scheme) : Declaration(location)

data class TypeDeclaration(
        override val location: Location,
        val name: ID,
        val scheme: Scheme,
        val constructors: List<Constructor>) : Declaration(location)

data class Constructor(
        override val location: Location,
        val name: ID,
        val arguments: List<Type>) : Node(location)


sealed class ValueDeclarationID(
        location: Location,
        open val name: ID) : Node(location)

data class LowerIDDeclarationID(
        override val location: Location,
        override val name: ID) : ValueDeclarationID(location, name)

data class OperatorDeclarationID(
        override val location: Location,
        override val name: ID,
        val precedence: Int,
        val associativity: Associativity) : ValueDeclarationID(location, name)


data class ID(
        override val location: Location,
        val name: String) : Node(location)


sealed class Expression(
        location: Location,
        open val type: Type) : Node(location)

data class Unit(
        override val location: Location,
        override val type: Type) : Expression(location, type)

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

data class ConstantChar(
        override val location: Location,
        override val type: Type,
        val value: Char) : Expression(location, type)

data class ConstantRecord(
        override val location: Location,
        override val type: Type,
        val fields: List<ConstantField>) : Expression(location, type)

data class ConstantField(
        override val location: Location,
        val name: ID,
        val value: Expression) : Node(location)

data class IdReference(
        override val location: Location,
        override val type: Type,
        val name: String) : Expression(location, type)

data class LetExpression(
        override val location: Location,
        override val type: Type,
        val declarations: List<LetDeclaration>,
        val expression: Expression) : Expression(location, type)

data class IfExpression(
        override val location: Location,
        override val type: Type,
        val guardExpression: Expression,
        val thenExpression: Expression,
        val elseExpression: Expression) : Expression(location, type)

data class LambdaExpression(
        override val location: Location,
        override val type: Type,
        val argument: Pattern,
        val expression: Expression) : Expression(location, type)

data class CallExpression(
        override val location: Location,
        override val type: Type,
        val operator: Expression,
        val operand: Expression) : Expression(location, type)

data class FieldProjectionExpression(
        override val location: Location,
        override val type: Type,
        val record: Expression,
        val name: ID): Expression(location, type)

data class UpdateRecordExpression(
        override val location: Location,
        override val type: Type,
        val record: Expression,
        val updates: List<Pair<ID, Expression>>) : Expression(location, type)

data class CaseExpression(
        override val location: Location,
        override val type: Type,
        val operator: Expression,
        val items: List<CaseItem>) : Expression(location, type)

data class CaseItem(
        override val location: Location,
        val pattern: Pattern,
        val expression: Expression) : Node(location)


sealed class Pattern(
        override val location: Location,
        open val type: Type) : Node(location)

data class ConstantIntPattern(
        override val location: Location,
        override val type: Type,
        val value: Int) : Pattern(location, type)

data class ConstantBoolPattern(
        override val location: Location,
        override val type: Type,
        val value: Boolean) : Pattern(location, type)

data class ConstantStringPattern(
        override val location: Location,
        override val type: Type,
        val value: String) : Pattern(location, type)

data class ConstantCharPattern(
        override val location: Location,
        override val type: Type,
        val value: Char) : Pattern(location, type)

data class ConstantUnitPattern(
        override val location: Location,
        override val type: Type) : Pattern(location, type)

data class IdReferencePattern(
        override val location: Location,
        override val type: Type,
        val name: String) : Pattern(location, type)

data class IgnorePattern(
        override val location: Location,
        override val type: Type) : Pattern(location, type)


data class ConstructorReferencePattern(
        override val location: Location,
        override val type: Type,
        val name: String,
        val parameters: List<Pattern>) : Pattern(location, type)
