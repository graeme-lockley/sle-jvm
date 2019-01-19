package za.co.no9.sle.transform.typelessToTypelessPattern

import za.co.no9.sle.*
import za.co.no9.sle.ast.typeless.False
import za.co.no9.sle.ast.typeless.LetSignature
import za.co.no9.sle.ast.typeless.NotExpression
import za.co.no9.sle.ast.typeless.True
import za.co.no9.sle.ast.typelessPattern.*
import za.co.no9.sle.parser.Lexer
import za.co.no9.sle.parser.parseModule


fun parse(text: String): Either<Errors, Module> =
        parseModule(Lexer(text))
                .andThen { transform(it) }


fun transform(ast: za.co.no9.sle.ast.typeless.Module): Either<Errors, Module> {
    val transformer =
            Transformer()

    val result =
            transformer.transform(ast)

    return if (transformer.errors.isEmpty())
        value(result)
    else
        error(transformer.errors)
}


private class Transformer {
    val errors =
            mutableSetOf<Error>()


    fun transform(ast: za.co.no9.sle.ast.typeless.Module): Module {
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

        val typeAndAliasDeclarations =
                ast.declarations
                        .filter { declaration -> declaration is za.co.no9.sle.ast.typeless.TypeDeclaration || declaration is za.co.no9.sle.ast.typeless.TypeAliasDeclaration }
                        .map { declaration ->
                            when (declaration) {
                                is za.co.no9.sle.ast.typeless.TypeDeclaration ->
                                    TypeDeclaration(
                                            declaration.location,
                                            transform(declaration.name),
                                            declaration.arguments.map { transform(it) },
                                            declaration.constructors.map { constructor -> Constructor(constructor.location, transform(constructor.name), constructor.arguments.map { transform(it) }) })

                                is za.co.no9.sle.ast.typeless.TypeAliasDeclaration ->
                                    TypeAliasDeclaration(declaration.location, transform(declaration.name), transform(declaration.type))

                                else ->
                                    throw Exception("Illegal outcome")
                            }

                        }

        val letDeclarations =
                transformComponentLetDeclarations(ast.declarations)

        return Module(ast.location, exports, imports, typeAndAliasDeclarations + letDeclarations)
    }


    private fun letNames(declarations: List<za.co.no9.sle.ast.typeless.Declaration>): Set<String> =
            declarations.fold(emptySet()) { names, declaration ->
                when (declaration) {
                    is za.co.no9.sle.ast.typeless.LetDeclaration ->
                        names + declaration.id.name.name

                    is za.co.no9.sle.ast.typeless.LetGuardDeclaration ->
                        names + declaration.id.name.name

                    else ->
                        names
                }
            }


    private fun letSignatureDictionary(declarations: List<za.co.no9.sle.ast.typeless.Declaration>): Map<String, LetSignature> {
        val letDeclarationNames =
                letNames(declarations)

        return declarations.fold(emptyMap()) { letSignatureDict, declaration ->
            when (declaration) {
                is LetSignature -> {
                    val nameOfLetSignature =
                            declaration.id.name.name

                    if (letDeclarationNames.contains(nameOfLetSignature)) {
                        val other =
                                letSignatureDict[nameOfLetSignature]

                        if (other != null) {
                            errors.add(DuplicateLetSignature(declaration.location, other.location, nameOfLetSignature))
                        }
                        letSignatureDict + Pair(nameOfLetSignature, declaration)
                    } else {
                        errors.add(LetSignatureWithoutDeclaration(declaration.location, nameOfLetSignature))
                        letSignatureDict
                    }
                }

                else ->
                    letSignatureDict
            }
        }
    }


    private fun transformComponentLetDeclarations(declarations: List<za.co.no9.sle.ast.typeless.Declaration>): List<LetDeclaration> {
        val signatureDictionary: Map<String, LetSignature> =
                letSignatureDictionary(declarations)

        return declarations
                .filter { ast -> ast is za.co.no9.sle.ast.typeless.LetDeclaration || ast is za.co.no9.sle.ast.typeless.LetGuardDeclaration }
                .groupBy { ast ->
                    when (ast) {
                        is za.co.no9.sle.ast.typeless.LetDeclaration ->
                            ast.id.name.name

                        is za.co.no9.sle.ast.typeless.LetGuardDeclaration ->
                            ast.id.name.name

                        else ->
                            ""
                    }
                }
                .map { asts ->
                    val ast0 =
                            asts.value[0]

                    val declarationID =
                            when (ast0) {
                                is za.co.no9.sle.ast.typeless.LetDeclaration ->
                                    ast0.id

                                is za.co.no9.sle.ast.typeless.LetGuardDeclaration ->
                                    ast0.id

                                else ->
                                    null
                            }!!

                    val location =
                            asts.value.fold(declarationID.location) { a, b -> a + b.location }

                    LetDeclaration(
                            locationFrom(asts.value)!!,
                            transform(declarationID),
                            transformNullable(signatureDictionary[asts.key]?.type),
                            asts.value.map { letDeclaration ->
                                when (letDeclaration) {
                                    is za.co.no9.sle.ast.typeless.LetDeclaration ->
                                        letDeclaration.arguments.foldRight(transform(letDeclaration.expression)) { name, expression -> LambdaExpression(location, transform(name), expression) }

                                    is za.co.no9.sle.ast.typeless.LetGuardDeclaration ->
                                        letDeclaration.arguments.foldRight(
                                                letDeclaration.guardedExpressions.dropLast(1).foldRight(
                                                        transform(letDeclaration.guardedExpressions.last().second)
                                                ) { a, b ->
                                                    IfExpression(location, transform(a.first), transform(a.second), b)
                                                }
                                        ) { name, expression -> LambdaExpression(location, transform(name), expression) }

                                    else ->
                                        throw Exception("Illegal outcome")
                                }
                            })
                }
    }


    private fun transform(valueDeclarationID: za.co.no9.sle.ast.typeless.ValueDeclarationID): ValueDeclarationID =
            when (valueDeclarationID) {
                is za.co.no9.sle.ast.typeless.LowerIDDeclarationID ->
                    LowerIDDeclarationID(valueDeclarationID.location, transform(valueDeclarationID.name))

                is za.co.no9.sle.ast.typeless.OperatorDeclarationID ->
                    OperatorDeclarationID(valueDeclarationID.location, transform(valueDeclarationID.name), valueDeclarationID.precedence, valueDeclarationID.associativity)
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

                is za.co.no9.sle.ast.typeless.OperatorNamedDeclaration ->
                    OperatorImportDeclaration(it.location, transform(it.name))

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
                is True ->
                    ConstantBool(ast.location, true)

                is False ->
                    ConstantBool(ast.location, false)

                is za.co.no9.sle.ast.typeless.ConstantInt ->
                    ConstantInt(ast.location, ast.value)

                is za.co.no9.sle.ast.typeless.ConstantString ->
                    ConstantString(ast.location, ast.value)

                is za.co.no9.sle.ast.typeless.ConstantChar ->
                    ConstantChar(ast.location, ast.value)

                is za.co.no9.sle.ast.typeless.ConstantList -> {
                    val initial: Expression =
                            IdReference(ast.location, QualifiedID(ast.location, null, "Nil"))

                    ast.expressions.foldRight(initial) { a, b ->
                        BinaryOpExpression(b.location, transform(a), ID(a.location, "::"), b)
                    }
                }

                is za.co.no9.sle.ast.typeless.ConstantRecord ->
                    TODO("Record")

                is NotExpression ->
                    CallExpression(ast.location, IdReference(ast.location, QualifiedID(ast.location, null, "(!)")), transform(ast.expression))

                is za.co.no9.sle.ast.typeless.IdReference ->
                    IdReference(ast.location, transform(ast.name))

                is za.co.no9.sle.ast.typeless.ConstructorReference ->
                    ConstructorReference(ast.location, transform(ast.name))

                is za.co.no9.sle.ast.typeless.LetExpression ->
                    LetExpression(ast.location, transformComponentLetDeclarations(ast.declarations), transform(ast.expression))

                is za.co.no9.sle.ast.typeless.IfExpression ->
                    IfExpression(ast.location, transform(ast.guardExpression), transform(ast.thenExpression), transform(ast.elseExpression))

                is za.co.no9.sle.ast.typeless.LambdaExpression ->
                    ast.arguments.foldRight(transform(ast.expression)) { name, expression -> LambdaExpression(ast.location, transform(name), expression) }

                is za.co.no9.sle.ast.typeless.BinaryOpExpression ->
                    BinaryOpExpression(ast.location, transform(ast.left), transform(ast.operator), transform(ast.right))

                is za.co.no9.sle.ast.typeless.NestedExpressions ->
                    NestedExpressions(ast.location, ast.expressions.map { transform(it) })

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

                is za.co.no9.sle.ast.typeless.ConstantCharPattern ->
                    ConstantCharPattern(pattern.location, pattern.value)

                is za.co.no9.sle.ast.typeless.ConstantNTuplePattern ->
                    ConstantNTuplePattern(pattern.location, pattern.patterns.map { transform(it) })

                is za.co.no9.sle.ast.typeless.ConstantListPattern ->
                    pattern.values.foldRight(ConstructorReferencePattern(pattern.location, QualifiedID(pattern.location, null, "Nil"), emptyList())) { carPattern, patternList ->
                        ConstructorReferencePattern(carPattern.location, QualifiedID(carPattern.location, null, "Cons"), listOf(transform(carPattern), patternList))
                    }

                is za.co.no9.sle.ast.typeless.ConsOperatorPattern ->
                    ConstructorReferencePattern(pattern.location, QualifiedID(pattern.location, null, "Cons"), listOf(transform(pattern.head), transform(pattern.tail)))

                is za.co.no9.sle.ast.typeless.IdReferencePattern ->
                    IdReferencePattern(pattern.location, pattern.name)

                is za.co.no9.sle.ast.typeless.IgnorePattern ->
                    IgnorePattern(pattern.location)

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
                is za.co.no9.sle.ast.typeless.TNTuple ->
                    TNTuple(type.location, type.types.map { transform(it) })

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
}