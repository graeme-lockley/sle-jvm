package za.co.no9.sle.ast.typedPattern

import za.co.no9.sle.Location
import za.co.no9.sle.typing.Scheme
import za.co.no9.sle.typing.Type


sealed class Node(
        open val location: Location)


data class Module(
        override val location: Location,
        val exports: List<ExportDeclaration>,
        val imports: List<Import>,
        val declarations: List<Declaration>) : Node(location)


sealed class ExportDeclaration(
        open val name: String)

data class ValueExportDeclaration(
        override val name: String,
        val scheme: Scheme) : ExportDeclaration(name)

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



data class Import(
        override val location: Location,
        val resourceName: String,
        val asName: ID,
        val namedDeclarations: List<ImportDeclaration>) : Node(location)

sealed class ImportDeclaration(
        location: Location) : Node(location)

data class ValueImportDeclaration(
        override val location: Location,
        val name: ID,
        val scheme: Scheme) : ImportDeclaration(location)

data class AliasImportDeclaration(
        override val location: Location,
        val name: ID,
        val scheme: Scheme) : ImportDeclaration(location)

data class ADTImportDeclaration(
        override val location: Location,
        val name: ID) : ImportDeclaration(location)

data class FullADTImportDeclaration(
        override val location: Location,
        val name: ID,
        val constructors: List<ConstructorImportDeclaration>) : ImportDeclaration(location)

data class ConstructorImportDeclaration(
        val name: String,
        val scheme: Scheme)


sealed class Declaration(
        location: Location) : Node(location)

data class LetDeclaration(
        override val location: Location,
        val scheme: Scheme,
        val name: ID,
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
        val argument: Pattern,
        val expression: Expression) : Expression(location, type)

data class CallExpression(
        override val location: Location,
        override val type: Type,
        val operator: Expression,
        val operand: Expression) : Expression(location, type)

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

data class ConstantUnitPattern(
        override val location: Location,
        override val type: Type) : Pattern(location, type)

data class IdReferencePattern(
        override val location: Location,
        override val type: Type,
        val name: String) : Pattern(location, type)

data class ConstructorReferencePattern(
        override val location: Location,
        override val type: Type,
        val name: String,
        val parameters: List<Pattern>) : Pattern(location, type)
