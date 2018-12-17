package za.co.no9.sle.transform.typelessToTypelessPattern

import za.co.no9.sle.*
import za.co.no9.sle.ast.typeless.False
import za.co.no9.sle.ast.typeless.LetSignature
import za.co.no9.sle.ast.typeless.NotExpression
import za.co.no9.sle.ast.typeless.True
import za.co.no9.sle.ast.typelessPattern.*
import za.co.no9.sle.ast.typelessPattern.Unit
import za.co.no9.sle.parser.Lexer
import za.co.no9.sle.parser.parseModule


fun parse(text: String): Either<Errors, Module> =
        parseModule(Lexer(text))
                .andThen { transform(it) }


private fun transform(ast: za.co.no9.sle.ast.typeless.Module): Either<Errors, Module> {
    val exports =
            ast.exports.map {
                when (it) {
                    is za.co.no9.sle.ast.typeless.LetExport ->
                        LetExport(it.location, transform(it.name))

                    is za.co.no9.sle.ast.typeless.TypeExport ->
                        TypeExport(it.location, transform(it.name), it.withConstructors)
                }
            }


    val imports =
            ast.imports.map { transform(it) }


    val letDeclarationNames =
            ast.declarations.fold(emptySet<String>()) { names, declaration ->
                when (declaration) {
                    is za.co.no9.sle.ast.typeless.LetDeclaration ->
                        names + declaration.id.name

                    is za.co.no9.sle.ast.typeless.LetGuardDeclaration ->
                        names + declaration.id.name

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
                                    declaration.id.name

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
        val typeAndAliasDeclarations =
                ast.declarations
                        .filter { ast -> ast is za.co.no9.sle.ast.typeless.TypeDeclaration || ast is za.co.no9.sle.ast.typeless.TypeAliasDeclaration }
                        .map { ast ->
                            when (ast) {
                                is za.co.no9.sle.ast.typeless.TypeDeclaration ->
                                    TypeDeclaration(
                                            ast.location,
                                            transform(ast.name),
                                            ast.arguments.map { transform(it) },
                                            ast.constructors.map { constructor -> Constructor(constructor.location, transform(constructor.name), constructor.arguments.map { transform(it) }) })

                                is za.co.no9.sle.ast.typeless.TypeAliasDeclaration ->
                                    TypeAliasDeclaration(ast.location, transform(ast.name), transform(ast.type))

                                else ->
                                    throw Exception("Illegal outcome")
                            }

                        }

        val letDeclarations =
                ast.declarations
                        .filter { ast -> ast is za.co.no9.sle.ast.typeless.LetDeclaration || ast is za.co.no9.sle.ast.typeless.LetGuardDeclaration }
                        .groupBy { ast ->
                            when (ast) {
                                is za.co.no9.sle.ast.typeless.LetDeclaration ->
                                    ast.id.name

                                is za.co.no9.sle.ast.typeless.LetGuardDeclaration ->
                                    ast.id.name

                                else ->
                                    ""
                            }
                        }
                        .map { asts ->
                            val ast0 =
                                    asts.value[0]

                            val name =
                                    when (ast0) {
                                        is za.co.no9.sle.ast.typeless.LetDeclaration ->
                                            ast0.id

                                        is za.co.no9.sle.ast.typeless.LetGuardDeclaration ->
                                            ast0.id

                                        else ->
                                            za.co.no9.sle.ast.typeless.ID(ast0.location, "")
                                    }

                            LetDeclaration(
                                    locationFrom(asts.value)!!,
                                    transform(name),
                                    transformNullable(it[asts.key]?.type),
                                    asts.value.map { letDeclaration ->
                                        when (letDeclaration) {
                                            is za.co.no9.sle.ast.typeless.LetDeclaration ->
                                                letDeclaration.arguments.foldRight(transform(letDeclaration.expression)) { name, expression -> LambdaExpression(ast.location, transform(name), expression) }

                                            is za.co.no9.sle.ast.typeless.LetGuardDeclaration ->
                                                letDeclaration.arguments.foldRight(
                                                        letDeclaration.guardedExpressions.dropLast(1).foldRight(
                                                                transform(letDeclaration.guardedExpressions.last().second)
                                                        ) { a, b ->
                                                            IfExpression(ast.location, transform(a.first), transform(a.second), b)
                                                        }
                                                ) { name, expression -> LambdaExpression(ast.location, transform(name), expression) }
                                            else ->
                                                throw Exception("Illegal outcome")

                                        }
                                    })
                        }

        Module(ast.location, exports, imports, typeAndAliasDeclarations + letDeclarations)
    }
}


private fun transform(import: za.co.no9.sle.ast.typeless.Import): Import {
    val urn =
            URN(import.urn.name)

    val asName =
            if (import.asName == null)
                if (import.namedDeclarations.isEmpty())
                    ID(import.urn.location, urn.impliedName())
                else
                    null
            else
                transform(import.asName)

    return Import(import.location, urn, asName, import.namedDeclarations.map {
        when (it) {
            is za.co.no9.sle.ast.typeless.LetNamedDeclaration ->
                ValueImportDeclaration(it.location, transform(it.name))

            is za.co.no9.sle.ast.typeless.TypeNamedDeclaration ->
                TypeImportDeclaration(it.location, transform(it.name), it.withConstructors)
        }
    })
}


private fun transform(ast: za.co.no9.sle.ast.typeless.ID): ID =
        ID(ast.location, ast.name)


private fun transform(qualifiedID: za.co.no9.sle.ast.typeless.QualifiedID): QualifiedID =
        QualifiedID(qualifiedID.location, qualifiedID.qualifier, qualifiedID.name)


private fun transform(ast: za.co.no9.sle.ast.typeless.Expression): Expression =
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
                CallExpression(ast.location, IdReference(ast.location, QualifiedID(ast.location, null, "(!)")), transform(ast.expression))

            is za.co.no9.sle.ast.typeless.IdReference ->
                IdReference(ast.location, transform(ast.name))

            is za.co.no9.sle.ast.typeless.ConstructorReference ->
                ConstructorReference(ast.location, transform(ast.name))

            is za.co.no9.sle.ast.typeless.IfExpression ->
                IfExpression(ast.location, transform(ast.guardExpression), transform(ast.thenExpression), transform(ast.elseExpression))

            is za.co.no9.sle.ast.typeless.LambdaExpression ->
                ast.arguments.foldRight(transform(ast.expression)) { name, expression -> LambdaExpression(ast.location, transform(name), expression) }

            is za.co.no9.sle.ast.typeless.BinaryOpExpression ->
                BinaryOpExpression(ast.location, transform(ast.left), transform(ast.operator), transform(ast.right))

            is za.co.no9.sle.ast.typeless.NestedExpression ->
                NestedExpression(ast.location, transform(ast.expression))

            is za.co.no9.sle.ast.typeless.CallExpression ->
                ast.operands.fold(transform(ast.operator)) { expression, operand -> CallExpression(ast.location, expression, transform(operand)) }

            is za.co.no9.sle.ast.typeless.CaseExpression ->
                CaseExpression(ast.location, transform(ast.operator), ast.items.map { item -> CaseItem(item.location, transform(item.pattern), transform(item.expression)) })
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
                ConstructorReferencePattern(pattern.location, transform(pattern.name), pattern.parameters.map { transform(it) })
        }


private fun transformNullable(type: za.co.no9.sle.ast.typeless.TType?): TType? =
        if (type == null)
            null
        else
            transform(type)

private fun transform(type: za.co.no9.sle.ast.typeless.TType): TType =
        when (type) {
            is za.co.no9.sle.ast.typeless.TUnit ->
                TUnit(type.location)

            is za.co.no9.sle.ast.typeless.TVarReference ->
                TVarReference(type.location, type.name)

            is za.co.no9.sle.ast.typeless.TTypeReference ->
                TTypeReference(type.location, transform(type.name), type.arguments.map { transform(it) })

            is za.co.no9.sle.ast.typeless.TArrow ->
                TArrow(type.location, transform(type.domain), transform(type.range))
        }


private fun locationFrom(nodes: List<za.co.no9.sle.ast.typeless.Node>): Location? =
        when {
            nodes.isEmpty() ->
                null

            else -> {
                var current =
                        nodes[0].location

                for (node in nodes) {
                    current += node.location
                }

                current
            }
        }
