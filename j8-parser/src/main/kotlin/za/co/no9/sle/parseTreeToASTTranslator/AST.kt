package za.co.no9.sle.parseTreeToASTTranslator

import org.antlr.v4.runtime.misc.Utils.spaces
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


sealed class TSchema(
        open val location: Location)

data class TIdReference(
        override val location: Location,
        val name: String) : TSchema(location)

data class TArrow(
        override val location: Location,
        val domain: TSchema,
        val range: TSchema) : TSchema(location)


fun toString(module: Module, indent: Int = 0): String =
        "${spaces(indent)}Module:\n" +
                "${spaces(indent + 2)}Location: ${module.location}\n" +
                module.declarations.joinToString("") { toString(it, indent + 2) }


fun toString(declaration: Declaration, indent: Int = 0): String =
        when (declaration) {
            is LetDeclaration ->
                "${spaces(indent)}LetDeclaration:\n" +
                        "${spaces(indent + 2)}Location: ${declaration.location}\n" +
                        "${spaces(indent + 2)}Name: ${declaration.name.name}\n" +
                        "${spaces(indent + 2)}Arguments: ${declaration.arguments.joinToString(", ")}\n" +
                        (if (declaration.schema == null)
                            ""
                        else
                            "${spaces(indent + 2)}Schema: ${toString(declaration.schema)}\n") +
                        "${spaces(indent + 2)}Expression:\n" +
                        toString(declaration.expression, indent + 4)

            is TypeAliasDeclaration ->
                "${spaces(indent)}TypeAliasDeclaration:\n" +
                        "${spaces(indent + 2)}Location: ${declaration.location}\n" +
                        "${spaces(indent + 2)}Name: ${declaration.name.name}\n" +
                        "${spaces(indent + 2)}Schema: ${toString(declaration.schema)}\n"
        }


fun toString(expression: Expression, indent: Int): String =
        when (expression) {
            is True ->
                "${spaces(indent)}True\n"

            is False ->
                "${spaces(indent)}False\n"

            is ConstantInt ->
                "${spaces(indent)}ConstantInt:\n" +
                        "${spaces(indent + 2)}Location: ${expression.location}\n" +
                        "${spaces(indent + 2)}Value: ${expression.value}\n"

            is ConstantString -> TODO()
            is NotExpression -> TODO()
            is IdReference ->
                "${spaces(indent)}IdReference:\n" +
                        "${spaces(indent + 2)}Location: ${expression.location}\n" +
                        "${spaces(indent + 2)}Name: ${expression.name}\n"

            is IfExpression -> TODO()
            is LambdaExpression -> TODO()
            is BinaryOpExpression ->
                "${spaces(indent)}BinaryOpExpression:\n" +
                        "${spaces(indent + 2)}Left:\n" +
                        toString(expression.left, indent + 4) +
                        "${spaces(indent + 2)}Operator: ${expression.operator}\n" +
                        "${spaces(indent + 2)}Right:\n" +
                        toString(expression.right, indent + 4)

            is CallExpression -> TODO()
        }


fun toString(schema: TSchema): String =
        when (schema) {
            is TIdReference ->
                schema.name

            is TArrow ->
                if (schema.domain is TArrow) {
                    "(${toString(schema.domain)}) -> ${toString(schema.range)}"
                } else {
                    "${toString(schema.domain)} -> ${toString(schema.range)}"
                }
        }
