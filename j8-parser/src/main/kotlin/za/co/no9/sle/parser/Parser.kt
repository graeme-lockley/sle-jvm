package za.co.no9.sle.parser

import za.co.no9.sle.*
import za.co.no9.sle.ast.typeless.*
import za.co.no9.sle.ast.typeless.Unit


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

        while (lexer.token != Token.EOF) {
            moduleDeclarations.add(parseDeclaration())
        }

        return Module(locationFrom(moduleDeclarations)!!, moduleDeclarations)
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

            isToken(Token.LowerID) -> {
                val id =
                        lexer.next().toID()

                if (isOperator(":")) {
                    matchOperator(":")

                    val type =
                            parseType()

                    return LetSignature(id.location + type.location, id, type)
                } else if (isToken(Token.LowerID) || isOperator("=") || isOperator("|")) {
                    val arguments =
                            mutableListOf<ID>()

                    while (isToken(Token.LowerID)) {
                        arguments.add(lexer.next().toID())
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
                throw syntaxError("Expected typealias, type or LowerID")
        }
    }


    fun parseTypeConstructor(): TypeConstructor {
        val upperID =
                matchToken(Token.UpperID, "Expected UpperID").toID()

        val arguments =
                mutableListOf<TType>()

        while (lexer.column > 2 && (
                        isToken(Token.LowerID) ||
                                isToken(Token.UpperID) ||
                                isOperator("(") ||
                                isOperator("()"))) {
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
                    parseMultiplicative(leftEdge)

            }


    fun parseMultiplicative(leftEdge: Int): Expression =
            parseBinaryOp(setOf("*", "/")) { parseAdditive(leftEdge) }

    fun parseAdditive(leftEdge: Int): Expression =
            parseBinaryOp(setOf("+", "-")) { parseRelationalOp(leftEdge) }

    fun parseRelationalOp(leftEdge: Int): Expression =
            parseBinaryOp(setOf("==", "!=", "<=", "<", ">=", ">")) { parseBooleanAnd(leftEdge) }

    fun parseBooleanAnd(leftEdge: Int): Expression =
            parseBinaryOp(setOf("&&")) { parseBooleanOr(leftEdge) }

    fun parseBooleanOr(leftEdge: Int): Expression =
            parseBinaryOp(setOf("||")) { parseLambda(leftEdge) }


    fun parseBinaryOp(operators: Set<String>, next: () -> Expression): Expression {
        val left =
                next()

        return if (isOperator(operators)) {
            val operator =
                    lexer.next()

            val right =
                    next()

            BinaryOpExpression(left.location + right.location, left, operator.toID(), right)
        } else {
            left
        }
    }

    fun parseLambda(leftEdge: Int): Expression =
            when {
                isOperator("\\") -> {
                    val lambdaSymbol =
                            lexer.next()

                    val arguments =
                            mutableListOf<ID>()

                    arguments.add(matchToken(Token.LowerID, "lower ID").toID())

                    while (isToken(Token.LowerID)) {
                        arguments.add(matchToken(Token.LowerID, "lower ID").toID())
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

                    IdReference(lowerIDSymbol.location, lowerIDSymbol.text)
                }

                isOperator("(") -> {
                    lexer.next()

                    val expression =
                            parseExpression(leftEdge)

                    matchOperator(")")

                    expression
                }

                isOperator("()") -> {
                    val unit =
                            lexer.next()

                    Unit(unit.location)
                }

                isToken(Token.UpperID) -> {
                    val lowerID =
                            lexer.next()

                    ConstructorReference(lowerID.location, lowerID.text)
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
                    isOperator("()")


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
                    val upperID =
                            lexer.next().toID()

                    val arguments =
                            mutableListOf<TType>()

                    while (lexer.column > 1 && isFirstADTType()) {
                        arguments.add(parseADTType())
                    }

                    TConstReference(upperID.location + locationFrom(arguments), upperID, arguments)
                }

                else ->
                    parseTermType()
            }


    fun isFirstADTType(): Boolean =
            isToken(Token.LowerID) ||
                    isToken(Token.UpperID) ||
                    isOperator("(") ||
                    isOperator("()")


    fun parseTermType(): TType {
        when {
            isToken(Token.LowerID) -> {
                val lowerID =
                        lexer.next()

                return TVarReference(lowerID.location, lowerID.text)
            }

            isOperator("(") -> {
                matchOperator("(")

                val type =
                        parseType()

                matchOperator(")")

                return type
            }

            isOperator("()") -> {
                val unitType =
                        lexer.next()

                return TUnit(unitType.location)
            }

            else -> {
                throw syntaxError("Expected lower ID, upper ID or '('")
            }
        }
    }


    fun parsePattern(): Pattern {
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

            isOperator("()") -> {
                val unitSymbol =
                        lexer.next()

                return ConstantUnitPattern(unitSymbol.location)

            }

            isOperator("(") -> {
                lexer.next()

                val pattern =
                        parsePattern()

                matchOperator(")")

                return pattern
            }

            isToken(Token.LowerID) -> {
                val lowerIDSymbol =
                        lexer.next()

                return IdReferencePattern(lowerIDSymbol.location, lowerIDSymbol.text)
            }

            isToken(Token.UpperID) -> {
                val upperIDSymbol =
                        lexer.next()

                val parameters =
                        mutableListOf<Pattern>()

                while (lexer.column > 1 && isFirstPattern()) {
                    parameters.add(parseConstructorArgument())
                }

                return ConstructorReferencePattern(upperIDSymbol.location + locationFrom(parameters), upperIDSymbol.text, parameters)
            }

            else ->
                throw syntaxError("Expected constant int, constant string, lower ID, upperID, True, False or '('")
        }
    }


    fun parseConstructorArgument(): Pattern {
        if (isToken(Token.UpperID)  && lexer.text != "True" && lexer.text != "False") {
            val upperIDSymbol =
                    lexer.next()

            return ConstructorReferencePattern(upperIDSymbol.location, upperIDSymbol.text, emptyList())
        } else {
            return parsePattern()
        }
    }


    fun isFirstPattern(): Boolean =
            isToken(Token.ConstantInt) ||
                    isToken(Token.ConstantString) ||
                    isToken(Token.UpperID) ||
                    isToken(Token.LowerID) ||
                    isOperator("()") ||
                    isOperator("(")


    private fun isOperator(text: String): Boolean =
            lexer.token == Token.ConstantOperator && lexer.text == text


    private fun isOperator(texts: Set<String>): Boolean =
            lexer.token == Token.ConstantOperator && texts.contains(lexer.text)


    private fun isToken(token: Token): Boolean =
            lexer.token == token


    private fun matchOperator(text: String): Symbol {
        if (isOperator(text)) {
            return lexer.next()
        } else {
            throw syntaxError("Expected '='")
        }
    }


    private fun matchToken(token: Token, text: String): Symbol {
        if (lexer.token == token) {
            return lexer.next()
        } else {
            throw syntaxError("Expected $text")
        }
    }


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