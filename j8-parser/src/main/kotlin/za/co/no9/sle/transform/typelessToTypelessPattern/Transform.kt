package za.co.no9.sle.transform.typelessToTypelessPattern

import za.co.no9.sle.*
import za.co.no9.sle.ast.typeless.*
import za.co.no9.sle.ast.typelessPattern.*
import za.co.no9.sle.ast.typelessPattern.CallExpression
import za.co.no9.sle.ast.typelessPattern.CaseExpression
import za.co.no9.sle.ast.typelessPattern.CaseItem
import za.co.no9.sle.ast.typelessPattern.ConstantBoolPattern
import za.co.no9.sle.ast.typelessPattern.ConstantInt
import za.co.no9.sle.ast.typelessPattern.ConstantIntPattern
import za.co.no9.sle.ast.typelessPattern.ConstantString
import za.co.no9.sle.ast.typelessPattern.ConstantStringPattern
import za.co.no9.sle.ast.typelessPattern.ConstantUnitPattern
import za.co.no9.sle.ast.typelessPattern.ConstructorReference
import za.co.no9.sle.ast.typelessPattern.ConstructorReferencePattern
import za.co.no9.sle.ast.typelessPattern.Expression
import za.co.no9.sle.ast.typelessPattern.ID
import za.co.no9.sle.ast.typelessPattern.IdReference
import za.co.no9.sle.ast.typelessPattern.IdReferencePattern
import za.co.no9.sle.ast.typelessPattern.IfExpression
import za.co.no9.sle.ast.typelessPattern.LambdaExpression
import za.co.no9.sle.ast.typelessPattern.LetDeclaration
import za.co.no9.sle.ast.typelessPattern.Module
import za.co.no9.sle.ast.typelessPattern.Pattern
import za.co.no9.sle.ast.typelessPattern.TypeAliasDeclaration
import za.co.no9.sle.ast.typelessPattern.TypeDeclaration
import za.co.no9.sle.ast.typelessPattern.Unit
import za.co.no9.sle.parser.Lexer
import za.co.no9.sle.parser.parseModule
import za.co.no9.sle.typing.*


fun parse(text: String): Either<Errors, Module> =
        parseModule(Lexer(text))
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
                            ast.arguments.foldIndexed(emptyMap<String, TVar>()) { index, subst, id -> subst + Pair(id.name, TVar(id.location, index)) }

                    val parameters =
                            ast.arguments.mapIndexed { index, _ -> index }

                    val scheme =
                            Scheme(parameters, TCon(ast.name.location, ast.name.name, ast.arguments.map { argument -> substitution[argument.name]!! }))

                    declarations + TypeDeclaration(
                            ast.location,
                            astToCoreAST(ast.name),
                            scheme,
                            ast.constructors.map { constructor -> Constructor(constructor.location, astToCoreAST(constructor.name), constructor.arguments.map { ttype -> astToType(ttype, substitution) }) }
                    )
                }

                is LetSignature ->
                    declarations

                is za.co.no9.sle.ast.typeless.LetDeclaration ->
//                    declarations + LetDeclaration(ast.location, astToCoreAST(ast.name), typeToScheme(it[ast.name.name]?.type), ast.arguments.foldRight(astToCoreAST(ast.expression)) { name, expression -> LambdaExpression(ast.location, astToCoreAST(name), expression) })
                    declarations + LetDeclaration(ast.location, astToCoreAST(ast.name), typeToScheme(it[ast.name.name]?.type), ast.arguments.foldRight(astToCoreAST(ast.expression)) { name, expression -> LambdaExpression(ast.location, transform(name), expression) })

                is za.co.no9.sle.ast.typeless.TypeAliasDeclaration ->
                    declarations + TypeAliasDeclaration(ast.location, astToCoreAST(ast.name), typeToScheme(ast.type)!!)

                is za.co.no9.sle.ast.typeless.LetGuardDeclaration ->
                    declarations + LetDeclaration(
                            ast.location,
                            astToCoreAST(ast.name),
                            typeToScheme(it[ast.name.name]?.type),
                            ast.arguments.foldRight(
                                    ast.guardedExpressions.dropLast(1).foldRight(
                                            astToCoreAST(ast.guardedExpressions.last().second)
                                    ) { a, b ->
                                        IfExpression(ast.location, astToCoreAST(a.first), astToCoreAST(a.second), b)
                                    }
                            ) { name, expression -> LambdaExpression(ast.location, transform(name), expression) })
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
                ast.arguments.foldRight(astToCoreAST(ast.expression)) { name, expression -> LambdaExpression(ast.location, transform(name), expression) }

            is BinaryOpExpression -> {
                val operator =
                        CallExpression(ast.operator.location, IdReference(ast.operator.location, "(${ast.operator.name})"), astToCoreAST(ast.left))

                val operand =
                        astToCoreAST(ast.right)

                CallExpression(ast.location, operator, operand)
            }

            is za.co.no9.sle.ast.typeless.CallExpression ->
                ast.operands.fold(astToCoreAST(ast.operator)) { expression, operand -> CallExpression(ast.location, expression, astToCoreAST(operand)) }

            is za.co.no9.sle.ast.typeless.CaseExpression ->
                CaseExpression(ast.location, astToCoreAST(ast.operator), ast.items.map { item -> CaseItem(item.location, transform(item.pattern), astToCoreAST(item.expression)) })
        }


private fun transform(pattern: za.co.no9.sle.ast.typeless.Pattern): Pattern =
        when (pattern) {
            is za.co.no9.sle.ast.typeless.ConstantIntPattern ->
                ConstantIntPattern(pattern.location, pattern.value)

            is za.co.no9.sle.ast.typeless.ConstantBoolPattern ->
                ConstantBoolPattern(pattern.location, pattern.value)

            is za.co.no9.sle.ast.typeless.ConstantStringPattern ->
                ConstantStringPattern(pattern.location, pattern.value)

            is za.co.no9.sle.ast.typeless.ConstantUnitPattern ->
                ConstantUnitPattern(pattern.location)

            is za.co.no9.sle.ast.typeless.IdReferencePattern ->
                IdReferencePattern(pattern.location, pattern.name)

            is za.co.no9.sle.ast.typeless.ConstructorReferencePattern ->
                ConstructorReferencePattern(pattern.location, pattern.name, pattern.parameters.map { transform(it) })
        }


private fun astToType(type: TType, substitution: Map<String, TVar> = emptyMap()): Type =
        when (type) {
            is TUnit ->
                typeUnit

            is TVarReference ->
                substitution[type.name] ?: TCon(type.location, type.name)

            is TConstReference ->
                TCon(type.location, type.name.name, type.arguments.map { astToType(it, substitution) })

            is TArrow ->
                TArr(astToType(type.domain, substitution), astToType(type.range, substitution))
        }


private fun typeToScheme(ttype: TType?): Scheme? {
    val pump =
            VarPump()

    val substitution =
            mutableMapOf<String, TVar>()


    fun map(ttype: TType): Type =
            when (ttype) {
                is TUnit ->
                    typeUnit

                is TVarReference -> {
                    val varRef =
                            substitution[ttype.name]

                    if (varRef == null) {
                        val newVarRef =
                                pump.fresh(ttype.location)

                        substitution[ttype.name] = newVarRef

                        newVarRef
                    } else {
                        varRef
                    }
                }

                is TConstReference ->
                    TCon(ttype.location, ttype.name.name, ttype.arguments.map { map(it) })

                is TArrow ->
                    TArr(map(ttype.domain), map(ttype.range))
            }


    return when (ttype) {
        null ->
            null

        else -> {
            val type =
                    map(ttype)

            Scheme(substitution.values.map { it.variable }, type)
        }
    }
}
