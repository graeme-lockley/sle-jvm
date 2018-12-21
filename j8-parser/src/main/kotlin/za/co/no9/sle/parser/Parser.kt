package za.co.no9.sle.parser

import za.co.no9.sle.*
import za.co.no9.sle.ast.typeless.*
import za.co.no9.sle.ast.typeless.Unit
import za.co.no9.sle.typing.Left
import za.co.no9.sle.typing.None
import za.co.no9.sle.typing.Right


fun parseModule(lexer: Lexer): Either<Errors, Module> {
    val parser =
            Parser(lexer)

    return try {
        value(parser.parseModule())
    } catch (e: Exception) {
        za.co.no9.sle.error(parser.errors)
    }
}


class Parser(private val lexer: Lexer) {
    var errors: Errors =
            setOf()

    fun parseModule(): Module {
        val moduleDeclarations =
                mutableListOf<Declaration>()

        val exports =
                if (lexer.token == Token.EXPORT)
                    parseExport()
                else
                    listOf()

        val imports =
                parseImports()

        while (lexer.token != Token.EOF) {
            moduleDeclarations.add(parseDeclaration())
        }

        return Module(homeLocation + locationFrom(moduleDeclarations) + locationFrom(exports) + locationFrom(imports), exports, imports, moduleDeclarations)
    }


    fun parseExport(): List<Export> {
        val exports =
                mutableListOf<Export>()

        matchToken(Token.EXPORT, "export expected")

        exports.add(parseExportedName())
        while (isOperator(",")) {
            skip()
            exports.add(parseExportedName())
        }

        return exports
    }


    fun parseExportedName(): Export =
            when {
                isToken(Token.LowerID) -> {
                    val lowerID =
                            lexer.next().toID()

                    LetExport(lowerID.location, lowerID)
                }

                isToken(Token.UpperID) -> {
                    val upperID =
                            lexer.next().toID()

                    if (isOperator("(")) {
                        skip()

                        matchOperator("..")

                        val closeParenthesis =
                                matchOperator(")")

                        TypeExport(upperID.location + closeParenthesis.location, upperID, true)
                    } else {
                        TypeExport(upperID.location, upperID, false)
                    }
                }

                isOperator("(") -> {
                    val openParen =
                            skip()

                    val op =
                            matchOperator()

                    val closeParen =
                            matchOperator(")")

                    LetExport(openParen.location + closeParen.location, op.toID())
                }

                else ->
                    throw syntaxError("Expected UpperID or LowerID")
            }


    fun parseImports(): List<Import> {
        val imports =
                mutableListOf<Import>()

        while (lexer.token == Token.IMPORT) {
            imports.add(parseImport())
        }

        return imports
    }


    fun parseImport(): Import {
        val importToken =
                matchToken(Token.IMPORT, "Expected import")

        val importURN =
                matchToken(Token.ImportURN, "Expected a import URN").toID()

        var lastLocation =
                importURN.location

        val asName = if (isToken(Token.AS)) {
            skip()
            val result =
                    matchToken(Token.UpperID, "Expected UpperID").toID()

            lastLocation =
                    result.location

            result
        } else
            null

        val declarations = if (isToken(Token.EXPOSING)) {
            val result =
                    mutableListOf<NamedDeclaration>()

            skip()

            result.add(parseImportName())
            while (isOperator(",")) {
                skip()
                result.add(parseImportName())
            }

            lastLocation =
                    result.last().location

            result
        } else
            listOf<NamedDeclaration>()


        return Import(importToken.location + lastLocation, importURN, asName, declarations)
    }


    fun parseImportName(): NamedDeclaration =
            when {
                isToken(Token.LowerID) -> {
                    val lowerID =
                            lexer.next()

                    LetNamedDeclaration(lowerID.location, lowerID.toID())
                }

                isToken(Token.UpperID) -> {
                    val upperID =
                            lexer.next().toID()

                    if (isOperator("(")) {
                        lexer.next()
                        matchOperator("..")
                        val closingParenthesis =
                                matchOperator(")")

                        TypeNamedDeclaration(upperID.location + closingParenthesis.location, upperID, true)
                    } else
                        TypeNamedDeclaration(upperID.location, upperID, false)
                }

                isOperator("(") -> {
                    val openParen =
                            skip()

                    val operator =
                            matchOperator()

                    val closeParen =
                            matchOperator(")")

                    OperatorNamedDeclaration(openParen.location + closeParen.location, operator.toID())
                }

                else ->
                    throw syntaxError("Expected UpperID, LowerID or '('")
            }


    fun parseDeclaration(): Declaration {
        when {
            isToken(Token.TYPE) -> {
                val typeSymbol =
                        lexer.next()

                val upperID =
                        matchToken(Token.UpperID, "Expected UpperID").toID()

                val arguments =
                        mutableListOf<ID>()

                while (isToken(Token.LowerID)) {
                    arguments.add(lexer.next().toID())
                }

                matchOperator("=")

                val typeConstructors =
                        mutableListOf<TypeConstructor>()

                typeConstructors.add(parseTypeConstructor())

                while (isOperator("|")) {
                    matchOperator("|")
                    typeConstructors.add(parseTypeConstructor())
                }

                return TypeDeclaration(typeSymbol.location + locationFrom(typeConstructors), upperID, arguments, typeConstructors)
            }

            isToken(Token.TYPEALIAS) -> {
                val typealiasSymbol =
                        lexer.next()

                val upperID =
                        matchToken(Token.UpperID, "Expected UpperID").toID()

                matchOperator("=")

                val type =
                        parseType()

                return TypeAliasDeclaration(typealiasSymbol.location + type.location, upperID, type)
            }

            isFirstValueDeclarationID() -> {
                val id =
                        parseValueDeclarationID()

                if (isOperator(":")) {
                    matchOperator(":")

                    val type =
                            parseType()

                    return LetSignature(id.location + type.location, id, type)
                } else if (isFirstArgumentPattern() || isOperator("=") || isOperator("|")) {
                    val arguments =
                            mutableListOf<Pattern>()

                    while (isFirstArgumentPattern()) {
                        arguments.add(parseArgumentPattern())
                    }

                    if (isOperator("|")) {
                        val guardedExpressions =
                                mutableListOf<Pair<Expression, Expression>>()

                        while (isOperator("|")) {
                            matchOperator("|")

                            val guard =
                                    parseExpression(id.column)

                            matchOperator("=")

                            val expression =
                                    parseExpression(id.column)

                            guardedExpressions.add(Pair(guard, expression))
                        }

                        return LetGuardDeclaration(id.location + locationFrom(guardedExpressions.map { it.second }), id, arguments, guardedExpressions)
                    } else {
                        matchOperator("=")

                        val expression =
                                parseExpression(id.column)

                        return LetDeclaration(locationFrom(listOf(id, expression))!!, id, arguments, expression)
                    }
                } else {
                    TODO()
                }
            }

            else ->
                throw syntaxError("Expected typealias, type, LowerID or '('")
        }
    }


    fun isFirstValueDeclarationID(): Boolean =
            isToken(Token.LowerID) || isOperator("(")


    fun parseValueDeclarationID(): ValueDeclarationID =
            when {
                isToken(Token.LowerID) -> {
                    val id =
                            lexer.next().toID()

                    LowerIDDeclarationID(id.location, id)
                }

                isOperator("(") ->
                    parseOperatorDeclarationID()

                else ->
                    throw syntaxError("Expected LowerID or '('")
            }


    fun parseOperatorDeclarationID(): OperatorDeclarationID {
        val openParen =
                matchOperator("(")

        val operator =
                matchOperator()

        val precedence =
                matchToken(Token.ConstantInt, "Constant Integer")

        val associativityToken =
                matchToken(Token.LowerID, "left, right or none")

        val associativity =
                when (associativityToken.text) {
                    "left" ->
                        Left

                    "right" ->
                        Right

                    "none" ->
                        None

                    else ->
                        throw syntaxError("Expected left, right or none rather than ${associativityToken.text}")
                }

        val closeParen =
                matchOperator(")")

        return OperatorDeclarationID(openParen.location + closeParen.location, operator.toID(), precedence.text.toInt(), associativity)
    }


    fun parseTypeConstructor(): TypeConstructor {
        val upperID =
                matchToken(Token.UpperID, "Expected UpperID").toID()

        val arguments =
                mutableListOf<TType>()

        while (lexer.column > 2 && (
                        isToken(Token.LowerID) ||
                                isToken(Token.UpperID) ||
                                isOperator("("))) {
            arguments.add(parseType())
        }

        return TypeConstructor(upperID.location + locationFrom(arguments), upperID, arguments)
    }


    fun parseExpression(leftEdge: Int): Expression =
            parseCaseExpression(leftEdge)


    fun parseCaseExpression(leftEdge: Int): Expression =
            when {
                isToken(Token.CASE) -> {
                    val caseSymbol =
                            lexer.next()

                    val expression =
                            parseExpression(leftEdge)

                    matchToken(Token.OF, "of")

                    val caseItems =
                            mutableListOf<CaseItem>()

                    while (lexer.column > caseSymbol.column && isFirstPattern()) {
                        val pattern =
                                parsePattern()

                        matchOperator("->")

                        val caseExpression =
                                parseExpression(pattern.column)

                        caseItems.add(CaseItem(pattern.location + caseExpression.location, pattern, caseExpression))
                    }

                    CaseExpression(caseSymbol.location + locationFrom(caseItems.map { it.expression }), expression, caseItems)
                }
                else ->
                    parseBinaryOperators(leftEdge)

            }


    private val excludeOperators =
            setOf("(", ")", "|", "=", ",", "[", "]")


    fun parseBinaryOperators(leftEdge: Int): Expression {
        val left =
                parseLambda(leftEdge)

        return if (isOperatorExcluded(excludeOperators)) {
            val operator =
                    lexer.next()

            val right =
                    parseBinaryOperators(leftEdge)

            BinaryOpExpression(left.location + right.location, left, operator.toID(), right)
        } else
            left
    }


    fun parseLambda(leftEdge: Int): Expression =
            when {
                isOperator("\\") -> {
                    val lambdaSymbol =
                            lexer.next()

                    val arguments =
                            mutableListOf<Pattern>()

                    arguments.add(parseArgumentPattern())

                    while (isFirstArgumentPattern()) {
                        arguments.add(parseArgumentPattern())
                    }

                    matchOperator("->")

                    val expression =
                            parseExpression(leftEdge)

                    LambdaExpression(lambdaSymbol.location + expression.location, arguments, expression)
                }

                else ->
                    parseIf(leftEdge)
            }


    fun parseIf(leftEdge: Int): Expression =
            when {
                isToken(Token.IF) -> {
                    val ifSymbol =
                            lexer.next()

                    val guardExpression =
                            parseExpression(leftEdge)

                    matchToken(Token.THEN, "then")

                    val thenExpression =
                            parseExpression(leftEdge)

                    matchToken(Token.ELSE, "else")

                    val elseExpression =
                            parseExpression(leftEdge)

                    IfExpression(ifSymbol.location + elseExpression.location, guardExpression, thenExpression, elseExpression)
                }

                else ->
                    parseCall(leftEdge)
            }


    fun parseCall(leftEdge: Int): Expression {
        val operator =
                parseTerm(leftEdge)

        val operands =
                mutableListOf<Expression>()

        while (lexer.column > leftEdge && isFirstTerm()) {
            operands.add(parseTerm(leftEdge))
        }

        return if (operands.isEmpty())
            operator
        else
            CallExpression(operator.location + locationFrom(operands), operator, operands)
    }


    fun parseTerm(leftEdge: Int): Expression =
            when {
                isToken(Token.ConstantInt) -> {
                    val token =
                            lexer.next()

                    ConstantInt(token.location, token.text.toInt())
                }

                isToken(Token.UpperID) && lexer.text == "True" ->
                    True(lexer.next().location)

                isToken(Token.UpperID) && lexer.text == "False" ->
                    False(lexer.next().location)

                isToken(Token.ConstantString) -> {
                    val constantStringSymbol =
                            lexer.next()

                    val text =
                            constantStringSymbol.text
                                    .drop(1)
                                    .dropLast(1)
                                    .replace("\\\\", "\\")
                                    .replace("\\\"", "\"")

                    ConstantString(constantStringSymbol.location, text)
                }

                isOperator("[]") -> {
                    val nilSymbol =
                            lexer.next()

                    IdReference(nilSymbol.location, QualifiedID(nilSymbol.location, null, "Nil"))
                }

                isOperator("[") -> {
                    val openSquare =
                            lexer.next()

                    val items =
                            mutableListOf<Expression>()

                    if (!isOperator("]")) {
                        items.add(parseExpression(leftEdge))

                        while (isOperator(",")) {
                            lexer.next()
                            items.add(parseExpression(leftEdge))
                        }
                    }

                    val endSquare =
                            matchOperator("]")

                    val initial: Expression =
                            IdReference(endSquare.location, QualifiedID(endSquare.location, null, "Nil"))

                    items.foldRight(initial) { a, b ->
                        BinaryOpExpression(b.location, a, ID(a.location, "::"), b)
                    }
                }

                isOperator("!") -> {
                    val bangSymbol =
                            lexer.next()

                    val expression =
                            parseExpression(leftEdge)

                    NotExpression(bangSymbol.location + expression.location, expression)
                }

                isToken(Token.LowerID) -> {
                    val lowerIDSymbol =
                            lexer.next()

                    IdReference(lowerIDSymbol.location, QualifiedID(lowerIDSymbol.location, null, lowerIDSymbol.text))
                }

                isOperator("(") -> {
                    val openParen =
                            lexer.next()

                    if (isOperator(")")) {
                        val closeParen =
                                lexer.next()

                        Unit(openParen.location + closeParen.location)
                    } else {
                        val expression =
                                parseExpression(leftEdge)

                        val closeParen =
                                matchOperator(")")

                        NestedExpression(openParen.location + closeParen.location, expression)
                    }
                }

                isToken(Token.UpperID) -> {
                    val upperID =
                            lexer.next()

                    if (isOperator(".")) {
                        skip()

                        when {
                            isToken(Token.UpperID) -> {
                                val name =
                                        lexer.next()

                                ConstructorReference(upperID.location + name.location, QualifiedID(upperID.location + name.location, upperID.text, name.text))
                            }

                            isToken(Token.LowerID) -> {
                                val name =
                                        lexer.next()

                                IdReference(upperID.location + name.location, QualifiedID(upperID.location + name.location, upperID.text, name.text))
                            }

                            else ->
                                throw syntaxError("Expected lower ID or upper ID")
                        }
                    } else {
                        ConstructorReference(upperID.location, QualifiedID(upperID.location, null, upperID.text))
                    }
                }

                else ->
                    TODO(lexer.next().toString())
            }

    fun isFirstTerm(): Boolean =
            isOperator("(") ||
                    isToken(Token.ConstantInt) ||
                    isToken(Token.ConstantString) ||
                    isOperator("!") ||
                    isToken(Token.LowerID) ||
                    isToken(Token.UpperID) ||
                    isOperator("[") ||
                    isOperator("[]")


    fun parseType(): TType {
        val bType =
                parseADTType()

        return if (isOperator("->")) {
            matchOperator("->")

            val type =
                    parseType()

            TArrow(bType.location + type.location, bType, type)
        } else {
            bType
        }
    }


    fun parseADTType(): TType =
            when {
                isToken(Token.UpperID) -> {
                    val qualifiedUpperID =
                            parseQualifiedUpperID()

                    val arguments =
                            mutableListOf<TType>()

                    while (lexer.column > 1 && isFirstADTType()) {
                        arguments.add(parseADTType())
                    }

                    TTypeReference(qualifiedUpperID.location + locationFrom(arguments), qualifiedUpperID, arguments)
                }

                else ->
                    parseTermType()
            }


    fun parseQualifiedUpperID(): QualifiedID =
            when {
                isToken(Token.UpperID) -> {
                    val upperID =
                            lexer.next().toID()

                    if (isOperator(".")) {
                        skip()
                        val otherUpperID =
                                lexer.next().toID()
                        QualifiedID(upperID.location + otherUpperID.location, upperID.name, otherUpperID.name)
                    } else {
                        QualifiedID(upperID.location, null, upperID.name)
                    }
                }

                else ->
                    throw syntaxError("Expected upper ID")
            }


    fun isFirstADTType(): Boolean =
            isToken(Token.LowerID) ||
                    isToken(Token.UpperID) ||
                    isOperator("(")


    fun parseTermType(): TType {
        when {
            isToken(Token.LowerID) -> {
                val lowerID =
                        lexer.next()

                return TVarReference(lowerID.location, lowerID.text)
            }

            isOperator("(") -> {
                val openParenSymbol =
                        matchOperator("(")

                return if (isOperator(")")) {
                    val closeParenSymbol =
                            matchOperator(")")

                    TUnit(openParenSymbol.location + closeParenSymbol.location)
                } else {
                    val type =
                            parseType()

                    matchOperator(")")

                    type
                }
            }

            else -> {
                throw syntaxError("Expected lower ID, upper ID or '('")
            }
        }
    }


    fun parsePattern(): Pattern =
            parseConstructorPattern()


    fun parseConstructorPattern(): Pattern =
            when {
                isToken(Token.UpperID) && lexer.text != "True" && lexer.text != "False" -> {
                    val qualifiedUpperID =
                            parseQualifiedUpperID()

                    val parameters =
                            mutableListOf<Pattern>()

                    while (lexer.column > 1 && isFirstPattern()) {
                        parameters.add(parseArgumentPattern())
                    }

                    ConstructorReferencePattern(qualifiedUpperID.location + locationFrom(parameters), qualifiedUpperID, parameters)
                }

                else ->
                    parseTermPattern()
            }


    fun parseArgumentPattern(): Pattern =
            when {
                isToken(Token.UpperID) && lexer.text != "True" && lexer.text != "False" -> {
                    val qualifiedUpperID =
                            parseQualifiedUpperID()

                    ConstructorReferencePattern(qualifiedUpperID.location, qualifiedUpperID, emptyList())
                }
                else -> parsePattern()
            }


    fun isFirstArgumentPattern(): Boolean =
            isToken(Token.UpperID) && lexer.text != "True" && lexer.text != "False" || isFirstPattern()


    fun parseTermPattern(): Pattern {
        when {
            isToken(Token.ConstantInt) -> {
                val constantIntSymbol =
                        lexer.next()

                return ConstantIntPattern(constantIntSymbol.location, constantIntSymbol.text.toInt())
            }

            isToken(Token.UpperID) && lexer.text == "True" -> {
                val trueSymbol =
                        lexer.next()

                return ConstantBoolPattern(trueSymbol.location, true)
            }

            isToken(Token.UpperID) && lexer.text == "False" -> {
                val trueSymbol =
                        lexer.next()

                return ConstantBoolPattern(trueSymbol.location, false)
            }

            isToken(Token.ConstantString) -> {
                val constantStringSymbol =
                        lexer.next()

                val text =
                        constantStringSymbol.text
                                .drop(1)
                                .dropLast(1)
                                .replace("\\\\", "\\")
                                .replace("\\\"", "\"")

                return ConstantStringPattern(constantStringSymbol.location, text)
            }

            isOperator("(") -> {
                val openParenSymbol =
                        lexer.next()

                return if (isOperator(")")) {
                    val closeParenSymbol =
                            lexer.next()

                    ConstantUnitPattern(openParenSymbol.location + closeParenSymbol.location)
                } else {
                    val pattern =
                            parsePattern()

                    matchOperator(")")

                    pattern
                }
            }

            isToken(Token.LowerID) -> {
                val lowerIDSymbol =
                        lexer.next()

                return IdReferencePattern(lowerIDSymbol.location, lowerIDSymbol.text)
            }

            else ->
                throw syntaxError("Expected constant int, constant string, lower ID, upperID, True, False or '('")
        }
    }


    fun isFirstPattern(): Boolean =
            isToken(Token.ConstantInt) ||
                    isToken(Token.ConstantString) ||
                    isToken(Token.UpperID) ||
                    isToken(Token.LowerID) ||
                    isOperator("(")


    private fun isOperator(text: String): Boolean =
            lexer.token == Token.ConstantOperator && lexer.text == text


    private fun isOperator(): Boolean =
            lexer.token == Token.ConstantOperator


    private fun isOperatorExcluded(exclusions: Set<String>): Boolean =
            lexer.token == Token.ConstantOperator && !exclusions.contains(lexer.text)


    private fun isToken(token: Token): Boolean =
            lexer.token == token


    private fun matchOperator(text: String): Symbol {
        if (isOperator(text)) {
            return lexer.next()
        } else {
            throw syntaxError("Expected '$text'")
        }
    }


    private fun matchOperator(): Symbol {
        if (isOperator()) {
            return lexer.next()
        } else {
            throw syntaxError("Expected an operator")
        }
    }


    private fun matchToken(token: Token, text: String): Symbol {
        if (lexer.token == token) {
            return lexer.next()
        } else {
            throw syntaxError("Expected $text")
        }
    }


    private fun skip() =
            lexer.next()


    private fun syntaxError(text: String): Exception {
        errors += SyntaxError(lexer.currentSymbol.location, text)
        return Exception("Syntax Error")
    }
}


private fun locationFrom(nodes: List<Node>): Location? =
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


private fun Symbol.toID(): ID =
        ID(this.location, this.text)