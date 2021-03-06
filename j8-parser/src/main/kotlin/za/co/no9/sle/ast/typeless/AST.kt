package za.co.no9.sle.ast.typeless

import za.co.no9.sle.Location
import za.co.no9.sle.typing.Associativity


sealed class Node(
        open val location: Location) {
    val column: Int
        get() = this.location.start.column
}


data class Module(
        override val location: Location,
        val exports: List<Export>,
        val imports: List<Import>,
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


data class Import(
        override val location: Location,
        val urn: ID,
        val asName: ID?,
        val namedDeclarations: List<NamedDeclaration>) : Node(location)


sealed class NamedDeclaration(
        location: Location) : Node(location)

data class LetNamedDeclaration(
        override val location: Location,
        val name: ID) : NamedDeclaration(location)

data class OperatorNamedDeclaration(
        override val location: Location,
        val name: ID) : NamedDeclaration(location)

data class TypeNamedDeclaration(
        override val location: Location,
        val name: ID,
        val withConstructors: Boolean) : NamedDeclaration(location)


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


sealed class Declaration(
        location: Location) : Node(location)


sealed class ComponentLetDeclaration(
        override val location: Location,
        open val id: ValueDeclarationID) : Declaration(location)

data class LetSignature(
        override val location: Location,
        override val id: ValueDeclarationID,
        val type: TType) : ComponentLetDeclaration(location, id)

data class LetDeclaration(
        override val location: Location,
        override val id: ValueDeclarationID,
        val arguments: List<Pattern>,
        val expression: Expression) : ComponentLetDeclaration(location, id)

data class LetGuardDeclaration(
        override val location: Location,
        override val id: ValueDeclarationID,
        val arguments: List<Pattern>,
        val guardedExpressions: List<Pair<Expression, Expression>>) : ComponentLetDeclaration(location, id)

data class TypeAliasDeclaration(
        override val location: Location,
        val name: ID,
        val type: TType) : Declaration(location)

data class TypeDeclaration(
        override val location: Location,
        val name: ID,
        val arguments: List<ID>,
        val constructors: List<TypeConstructor>) : Declaration(location)

data class TypeConstructor(
        override val location: Location,
        val name: ID,
        val arguments: List<TType>) : Node(location)


data class ID(
        override val location: Location,
        val name: String) : Node(location)

data class QualifiedID(
        override val location: Location,
        val qualifier: String?,
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

data class ConstantChar(
        override val location: Location,
        val value: Char) : Expression(location)

data class ConstantList(
        override val location: Location,
        val expressions: List<Expression>) : Expression(location)

data class ConstantRecord(
        override val location: Location,
        val fields: List<ConstantField>) : Expression(location)

data class ConstantField(
        override val location: Location,
        val name: ID,
        val value: Expression) : Node(location)

data class NotExpression(
        override val location: Location,
        val expression: Expression) : Expression(location)

data class IdReference(
        override val location: Location,
        val name: QualifiedID) : Expression(location)

data class ConstructorReference(
        override val location: Location,
        val name: QualifiedID) : Expression(location)

data class LetExpression(
        override val location: Location,
        val declarations: List<ComponentLetDeclaration>,
        val expression: Expression) : Expression(location)

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

data class NestedExpressions(
        override val location: Location,
        val expressions: List<Expression>) : Expression(location)

data class CallExpression(
        override val location: Location,
        val operator: Expression,
        val operands: List<Expression>) : Expression(location)

data class FieldProjectionExpression(
        override val location: Location,
        val record: Expression,
        val name: ID) : Expression(location)

data class UpdateRecordExpression(
        override val location: Location,
        val record: Expression,
        val updates: List<Pair<ID, Expression>>) : Expression(location)

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

data class ConstantCharPattern(
        override val location: Location,
        val value: Char) : Pattern(location)

data class ConstantNTuplePattern(
        override val location: Location,
        val patterns: List<Pattern>) : Pattern(location)

data class ConstantListPattern(
        override val location: Location,
        val values: List<Pattern>) : Pattern(location)

data class IdReferencePattern(
        override val location: Location,
        val name: String) : Pattern(location)

data class IgnorePattern(
        override val location: Location) : Pattern(location)

data class ConsOperatorPattern(
        override val location: Location,
        val head: Pattern,
        val tail: Pattern) : Pattern(location)

data class ConstructorReferencePattern(
        override val location: Location,
        val name: QualifiedID,
        val parameters: List<Pattern>) : Pattern(location)

data class RecordPattern(
        override val location: Location,
        val fields: List<Pair<ID, Pattern?>>) : Pattern(location)


sealed class TType(
        override val location: Location) : Node(location)

data class TNTuple(
        override val location: Location,
        val types: List<TType>) : TType(location)

data class TVarReference(
        override val location: Location,
        val name: String) : TType(location)

data class TTypeReference(
        override val location: Location,
        val name: QualifiedID,
        val arguments: List<TType>) : TType(location)

data class TArrow(
        override val location: Location,
        val domain: TType,
        val range: TType) : TType(location)

data class TRecord(
        override val location: Location,
        val fields: List<Pair<ID, TType>>) : TType(location)
