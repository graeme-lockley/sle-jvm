package za.co.no9.sle.astToCoreAST

import za.co.no9.sle.Either
import za.co.no9.sle.Errors
import za.co.no9.sle.map
import za.co.no9.sle.parseTreeToASTTranslator.*
import za.co.no9.sle.typing.Schema
import za.co.no9.sle.typing.TArr
import za.co.no9.sle.typing.TCon
import za.co.no9.sle.typing.Type


fun parse(text: String): Either<Errors, Module> =
        za.co.no9.sle.parseTreeToASTTranslator
                .parse(text)
                .map { astToCoreAST(it) }

fun astToCoreAST(ast: za.co.no9.sle.parseTreeToASTTranslator.Module): Module =
        Module(ast.location, ast.declarations.map { astToCoreAST(it) })


private fun astToCoreAST(ast: za.co.no9.sle.parseTreeToASTTranslator.Declaration): Declaration =
        when (ast) {
            is za.co.no9.sle.parseTreeToASTTranslator.LetDeclaration ->
                LetDeclaration(ast.location, astToCoreAST(ast.name), astToCoreASTOptional(ast.schema), ast.arguments.foldRight(astToCoreAST(ast.expression)) { name, expression -> LambdaExpression(ast.location, astToCoreAST(name), expression) })

            is za.co.no9.sle.parseTreeToASTTranslator.TypeAliasDeclaration ->
                TypeAliasDeclaration(ast.location, astToCoreAST(ast.name), astToCoreAST(ast.schema))
        }

fun astToCoreAST(ast: za.co.no9.sle.parseTreeToASTTranslator.ID): ID =
        ID(ast.location, ast.name)


fun astToCoreAST(ast: za.co.no9.sle.parseTreeToASTTranslator.Expression): Expression =
        when (ast) {
            is True ->
                ConstantBool(ast.location, true)

            is False ->
                ConstantBool(ast.location, false)

            is za.co.no9.sle.parseTreeToASTTranslator.ConstantInt ->
                ConstantInt(ast.location, ast.value)

            is za.co.no9.sle.parseTreeToASTTranslator.ConstantString ->
                ConstantString(ast.location, ast.value)

            is NotExpression ->
                CallExpression(ast.location, IdReference(ast.location, "(!)"), astToCoreAST(ast.expression))

            is za.co.no9.sle.parseTreeToASTTranslator.IdReference ->
                IdReference(ast.location, ast.name)

            is za.co.no9.sle.parseTreeToASTTranslator.IfExpression ->
                IfExpression(ast.location, astToCoreAST(ast.guardExpression), astToCoreAST(ast.thenExpression), astToCoreAST(ast.elseExpression))

            is za.co.no9.sle.parseTreeToASTTranslator.LambdaExpression ->
                ast.arguments.foldRight(astToCoreAST(ast.expression)) { name, expression -> LambdaExpression(ast.location, astToCoreAST(name), expression) }

            is BinaryOpExpression ->
                CallExpression(ast.location, CallExpression(ast.operator.location, IdReference(ast.operator.location, "(${ast.operator.name})"), astToCoreAST(ast.left)), astToCoreAST(ast.right))

            is za.co.no9.sle.parseTreeToASTTranslator.CallExpression ->
                ast.operands.fold(astToCoreAST(ast.operator)) { expression, operand -> CallExpression(ast.location, expression, astToCoreAST(operand)) }
        }


private fun astToCoreASTOptional(ast: TSchema?): Schema? {
    return when (ast) {
        null ->
            null

        else ->
            astToCoreAST(ast)
    }
}


private fun astToCoreAST(ast: TSchema): Schema {
    fun astToType(type: TType): Type =
            when (type) {
                is TIdReference ->
                    TCon(type.name)

                is TArrow ->
                    TArr(astToType(type.domain), astToType(type.range))

                is TBar ->
                    TODO()
            }

    return Schema(emptyList(), astToType(ast.type))
}
