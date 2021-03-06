package za.co.no9.sle.ast.core

import za.co.no9.sle.Location
import za.co.no9.sle.typing.Associativity
import za.co.no9.sle.typing.Scheme
import za.co.no9.sle.typing.Type


sealed class Node(
        open val location: Location)


data class Module(
        override val location: Location,
        val exports: List<NameDeclaration>,
        val declarations: List<Declaration>) : Node(location)


sealed class NameDeclaration(
        open val name: String)

data class ValueNameDeclaration(
        override val name: String,
        val scheme: Scheme) : NameDeclaration(name)

data class OperatorNameDeclaration(
        override val name: String,
        val scheme: Scheme,
        val precedence: Int,
        val associativity: Associativity) : NameDeclaration(name)

data class AliasNameDeclaration(
        override val name: String,
        val scheme: Scheme) : NameDeclaration(name)

data class ADTNameDeclaration(
        override val name: String,
        val scheme: Scheme) : NameDeclaration(name)

data class FullADTNameDeclaration(
        override val name: String,
        val scheme: Scheme,
        val constructors: List<ConstructorNameDeclaration>) : NameDeclaration(name)

data class ConstructorNameDeclaration(
        val name: String,
        val scheme: Scheme)


sealed class Declaration(
        location: Location) : Node(location)

data class LetDeclaration(
        override val location: Location,
        val scheme: Scheme,
        val id: ValueDeclarationID,
        val expression: Expression) : Declaration(location)

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

data class ConstantConstructor(
        override val location: Location,
        override val type: Type,
        val name: String,
        val fields: List<Expression>) : Expression(location, type)

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
        val argument: ID,
        val expression: Expression) : Expression(location, type)

data class CallExpression(
        override val location: Location,
        override val type: Type,
        val operator: Expression,
        val operand: Expression) : Expression(location, type)

data class ProjectionExpression(
        override val location: Location,
        override val type: Type,
        val record: Expression,
        val index: Int) : Expression(location, type)

data class CaseExpression(
        override val location: Location,
        override val type: Type,
        val variable: String,
        val clauses: List<CaseExpressionClause>) : Expression(location, type)

data class CaseExpressionClause(
        val constructorName: String,
        val variables: List<String>,
        val expression: Expression)

data class FAIL(
        override val location: Location,
        override val type: Type): Expression(location, type)

data class ERROR(
        override val location: Location,
        override val type: Type): Expression(location, type)

