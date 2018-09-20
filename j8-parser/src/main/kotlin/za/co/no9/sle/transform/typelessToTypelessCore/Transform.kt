package za.co.no9.sle.transform.typelessToTypelessCore

import za.co.no9.sle.*
import za.co.no9.sle.ast.typeless.*
import za.co.no9.sle.ast.typelessCore.*
import za.co.no9.sle.ast.typelessCore.CallExpression
import za.co.no9.sle.ast.typelessCore.ConstantInt
import za.co.no9.sle.ast.typelessCore.ConstantString
import za.co.no9.sle.ast.typelessCore.Expression
import za.co.no9.sle.ast.typelessCore.ID
import za.co.no9.sle.ast.typelessCore.IdReference
import za.co.no9.sle.ast.typelessCore.IfExpression
import za.co.no9.sle.ast.typelessCore.LambdaExpression
import za.co.no9.sle.ast.typelessCore.LetDeclaration
import za.co.no9.sle.ast.typelessCore.Module
import za.co.no9.sle.ast.typelessCore.TypeAliasDeclaration
import za.co.no9.sle.ast.typelessCore.Unit
import za.co.no9.sle.typing.*


fun parse(text: String): Either<Errors, Module> =
        za.co.no9.sle.transform.parseTreeToTypeless.parse(text)
                .andThen { astToCoreAST(it) }


fun astToCoreAST(ast: za.co.no9.sle.ast.typeless.Module): Either<Errors, Module> {
    val letDeclarationNames =
            ast.declarations.fold(emptySet<String>()) { names, ast ->
                when (ast) {
                    is za.co.no9.sle.ast.typeless.LetDeclaration ->
                        names + ast.name.name

                    is za.co.no9.sle.ast.typeless.LetGuardDeclaration ->
                        names + ast.name.name

                    else ->
                        names
                }
            }

    val letSignatureDict: Either<Errors, Map<String, LetSignature>> =
            ast.declarations.fold(value(emptyMap())) { letSignatureDict, ast ->
                when (ast) {
                    is LetSignature ->
                        letSignatureDict.andThen {
                            val nameOfLetSignature =
                                    ast.name.name

                            if (letDeclarationNames.contains(nameOfLetSignature)) {
                                val other =
                                        it[nameOfLetSignature]

                                if (other == null)
                                    value(it + Pair(nameOfLetSignature, ast))
                                else
                                    za.co.no9.sle.error(setOf(DuplicateLetSignature(ast.location, other.location, nameOfLetSignature)))
                            } else {
                                za.co.no9.sle.error(setOf(LetSignatureWithoutDeclaration(ast.location, nameOfLetSignature)))
                            }
                        }

                    else ->
                        letSignatureDict
                }
            }

    return letSignatureDict.map {
        Module(ast.location, ast.declarations.fold(emptyList()) { declarations, ast ->
            when (ast) {
                is LetSignature ->
                    declarations

                is za.co.no9.sle.ast.typeless.LetDeclaration ->
                    declarations + LetDeclaration(ast.location, astToCoreAST(ast.name), astToCoreASTOptional(it[ast.name.name]?.schema), ast.arguments.foldRight(astToCoreAST(ast.expression)) { name, expression -> LambdaExpression(ast.location, astToCoreAST(name), expression) })

                is za.co.no9.sle.ast.typeless.TypeAliasDeclaration ->
                    declarations + TypeAliasDeclaration(ast.location, astToCoreAST(ast.name), astToCoreAST(ast.schema))

                is za.co.no9.sle.ast.typeless.LetGuardDeclaration ->
                    declarations + LetDeclaration(
                            ast.location,
                            astToCoreAST(ast.name),
                            astToCoreASTOptional(it[ast.name.name]?.schema),
                            ast.arguments.foldRight(
                                    ast.guardedExpressions.dropLast(1).foldRight(
                                            astToCoreAST(ast.guardedExpressions.last().second)
                                    ) { a, b ->
                                        IfExpression(ast.location, astToCoreAST(a.first), astToCoreAST(a.second), b)
                                    }
                            ) { name, expression -> LambdaExpression(ast.location, astToCoreAST(name), expression) })
            }
        })
    }
}


fun astToCoreAST(ast: za.co.no9.sle.ast.typeless.ID): ID =
        ID(ast.location, ast.name)


fun astToCoreAST(ast: za.co.no9.sle.ast.typeless.Expression): Expression =
        when (ast) {
            is za.co.no9.sle.ast.typeless.Unit ->
                Unit(ast.location)

            is True ->
                ConstantBool(ast.location, true)

            is False ->
                ConstantBool(ast.location, false)

            is za.co.no9.sle.ast.typeless.ConstantInt ->
                ConstantInt(ast.location, ast.value)

            is za.co.no9.sle.ast.typeless.ConstantString ->
                ConstantString(ast.location, ast.value)

            is NotExpression ->
                CallExpression(ast.location, IdReference(ast.location, "(!)"), astToCoreAST(ast.expression))

            is za.co.no9.sle.ast.typeless.IdReference ->
                IdReference(ast.location, ast.name)

            is za.co.no9.sle.ast.typeless.IfExpression ->
                IfExpression(ast.location, astToCoreAST(ast.guardExpression), astToCoreAST(ast.thenExpression), astToCoreAST(ast.elseExpression))

            is za.co.no9.sle.ast.typeless.LambdaExpression ->
                ast.arguments.foldRight(astToCoreAST(ast.expression)) { name, expression -> LambdaExpression(ast.location, astToCoreAST(name), expression) }

            is BinaryOpExpression ->
                CallExpression(ast.location, CallExpression(ast.operator.location, IdReference(ast.operator.location, "(${ast.operator.name})"), astToCoreAST(ast.left)), astToCoreAST(ast.right))

            is za.co.no9.sle.ast.typeless.CallExpression ->
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
    val substitution =
            ast.parameters.map { it.name.name }.zip(ast.parameters.mapIndexed { index, _ -> TVar(index) }).toMap()


    fun astToType(type: TType): Type =
            when (type) {
                is TUnit ->
                    typeUnit

                is TIdReference ->
                    substitution[type.name] ?: TCon(type.name)

                is TArrow ->
                    TArr(astToType(type.domain), astToType(type.range))
            }


    return Schema(ast.parameters.mapIndexed { index, _ -> index }, astToType(ast.type))
}