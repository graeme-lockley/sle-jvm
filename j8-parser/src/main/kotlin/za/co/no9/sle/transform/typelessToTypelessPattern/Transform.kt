package za.co.no9.sle.transform.typelessToTypelessPattern

import za.co.no9.sle.*
import za.co.no9.sle.ast.typeless.*
import za.co.no9.sle.ast.typelessPattern.*
import za.co.no9.sle.ast.typelessPattern.CallExpression
import za.co.no9.sle.ast.typelessPattern.ConstantInt
import za.co.no9.sle.ast.typelessPattern.ConstantString
import za.co.no9.sle.ast.typelessPattern.ConstructorReference
import za.co.no9.sle.ast.typelessPattern.Expression
import za.co.no9.sle.ast.typelessPattern.ID
import za.co.no9.sle.ast.typelessPattern.IdReference
import za.co.no9.sle.ast.typelessPattern.IfExpression
import za.co.no9.sle.ast.typelessPattern.LambdaExpression
import za.co.no9.sle.ast.typelessPattern.LetDeclaration
import za.co.no9.sle.ast.typelessPattern.Module
import za.co.no9.sle.ast.typelessPattern.TypeAliasDeclaration
import za.co.no9.sle.ast.typelessPattern.TypeDeclaration
import za.co.no9.sle.ast.typelessPattern.Unit
import za.co.no9.sle.typing.*


fun parse(text: String): Either<Errors, Module> =
        za.co.no9.sle.transform.parseTreeToTypeless.parse(text)
                .andThen { astToCoreAST(it) }


fun astToCoreAST(ast: za.co.no9.sle.ast.typeless.Module): Either<Errors, Module> {
    val letDeclarationNames =
            ast.declarations.fold(emptySet<String>()) { names, declaration ->
                when (declaration) {
                    is za.co.no9.sle.ast.typeless.LetDeclaration ->
                        names + declaration.name.name

                    is za.co.no9.sle.ast.typeless.LetGuardDeclaration ->
                        names + declaration.name.name

                    else ->
                        names
                }
            }

    val letSignatureDict: Either<Errors, Map<String, LetSignature>> =
            ast.declarations.fold(value(emptyMap())) { letSignatureDict, declaration ->
                when (declaration) {
                    is LetSignature ->
                        letSignatureDict.andThen {
                            val nameOfLetSignature =
                                    declaration.name.name

                            if (letDeclarationNames.contains(nameOfLetSignature)) {
                                val other =
                                        it[nameOfLetSignature]

                                if (other == null)
                                    value(it + Pair(nameOfLetSignature, declaration))
                                else
                                    za.co.no9.sle.error(setOf(DuplicateLetSignature(declaration.location, other.location, nameOfLetSignature)))
                            } else {
                                za.co.no9.sle.error(setOf(LetSignatureWithoutDeclaration(declaration.location, nameOfLetSignature)))
                            }
                        }

                    else ->
                        letSignatureDict
                }
            }

    return letSignatureDict.map {
        Module(ast.location, ast.declarations.fold(emptyList()) { declarations, ast ->
            when (ast) {
                is za.co.no9.sle.ast.typeless.TypeDeclaration -> {
                    val substitution =
                            ast.arguments.foldIndexed(emptyMap<String, TVar>()) { index, subst, id -> subst + Pair(id.name, TVar(index)) }

                    val parameters =
                            ast.arguments.mapIndexed { index, _ -> index }

                    val scheme =
                            Scheme(parameters, TCon(ast.name.name, ast.arguments.map { substitution[it.name]!! }))

                    declarations + TypeDeclaration(
                            ast.location,
                            astToCoreAST(ast.name),
                            scheme,
                            ast.constructors.map { Constructor(it.location, astToCoreAST(it.name), it.arguments.map { ttype -> astToType(ttype, substitution) }) }
                    )
                }

                is LetSignature ->
                    declarations

                is za.co.no9.sle.ast.typeless.LetDeclaration ->
                    declarations + LetDeclaration(ast.location, astToCoreAST(ast.name), astToCoreASTOptional(it[ast.name.name]?.scheme), ast.arguments.foldRight(astToCoreAST(ast.expression)) { name, expression -> LambdaExpression(ast.location, astToCoreAST(name), expression) })

                is za.co.no9.sle.ast.typeless.TypeAliasDeclaration ->
                    declarations + TypeAliasDeclaration(ast.location, astToCoreAST(ast.name), astToCoreAST(ast.scheme))

                is za.co.no9.sle.ast.typeless.LetGuardDeclaration ->
                    declarations + LetDeclaration(
                            ast.location,
                            astToCoreAST(ast.name),
                            astToCoreASTOptional(it[ast.name.name]?.scheme),
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

            is za.co.no9.sle.ast.typeless.ConstructorReference ->
                ConstructorReference(ast.location, ast.name)

            is za.co.no9.sle.ast.typeless.IfExpression ->
                IfExpression(ast.location, astToCoreAST(ast.guardExpression), astToCoreAST(ast.thenExpression), astToCoreAST(ast.elseExpression))

            is za.co.no9.sle.ast.typeless.LambdaExpression ->
                ast.arguments.foldRight(astToCoreAST(ast.expression)) { name, expression -> LambdaExpression(ast.location, astToCoreAST(name), expression) }

            is BinaryOpExpression -> {
                val operator =
                        CallExpression(ast.operator.location, IdReference(ast.operator.location, "(${ast.operator.name})"), astToCoreAST(ast.left))

                val operand =
                        astToCoreAST(ast.right)

                CallExpression(ast.location, operator, operand)
            }

            is za.co.no9.sle.ast.typeless.CallExpression ->
                ast.operands.fold(astToCoreAST(ast.operator)) { expression, operand -> CallExpression(ast.location, expression, astToCoreAST(operand)) }
        }


private fun astToCoreASTOptional(ast: TScheme?): Scheme? {
    return when (ast) {
        null ->
            null

        else ->
            astToCoreAST(ast)
    }
}

private fun astToType(type: TType, substitution: Map<String, TVar> = emptyMap()): Type =
        when (type) {
            is TUnit ->
                typeUnit

            is TVarReference ->
                substitution[type.name] ?: TCon(type.name)

            is TConstReference ->
                TCon(type.name.name, type.arguments.map { astToType(it, substitution) })

            is TArrow ->
                TArr(astToType(type.domain, substitution), astToType(type.range, substitution))
        }

private fun astToCoreAST(ast: TScheme): Scheme {
    val substitution =
            ast.parameters.zip(ast.parameters.mapIndexed { index, _ -> TVar(index) }).toMap()

    return Scheme(ast.parameters.mapIndexed { index, _ -> index }, astToType(ast.type, substitution))
}
