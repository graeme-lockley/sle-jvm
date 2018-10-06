package za.co.no9.sle.transform.parseTreeToTypeless

import za.co.no9.sle.*
import za.co.no9.sle.ast.typeless.*
import za.co.no9.sle.ast.typeless.Unit
import za.co.no9.sle.parser.Lexer
import za.co.no9.sle.parser.Symbol
import za.co.no9.sle.parser.Token


fun parseModule(lexer: Lexer): Either<Errors, Module> {
    val parser =
            Parser(lexer)

    return try {
        value(parser.parseModule())
    } catch (e: Exception) {
        za.co.no9.sle.error(parser.errors)
    }
}


class Parser(val lexer: Lexer) {
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

    private fun fv(type: TType): Set<String> =
            when (type) {
                is TUnit ->
                    emptySet()

                is TVarReference ->
                    setOf(type.name)

                is TConstReference ->
                    emptySet()

                is TArrow ->
                    fv(type.domain) + fv(type.range)
            }


    private fun generaliseType(type: TType): TScheme =
            TScheme(type.location, fv(type).toList(), type)


    fun parseDeclaration(): Declaration {
        if (lexer.token == Token.TYPE) {
            val typeSymbol =
                    lexer.next()

            val upperID =
                    matchToken(Token.UpperID, "Expected UpperID").toID()

            val arguments =
                    mutableListOf<ID>()

            while (lexer.token == Token.LowerID) {
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
        } else if (lexer.token == Token.TYPEALIAS) {
            val typealiasSymbol =
                    lexer.next()

            val upperID =
                    matchToken(Token.UpperID, "Expected UpperID").toID()

            matchOperator("=")

            val type =
                    parseType()

            return TypeAliasDeclaration(typealiasSymbol.location + type.location, upperID, generaliseType(type))
        } else if (lexer.token == Token.LowerID) {
            val id =
                    lexer.next().toID()

            if (isOperator(":")) {
                matchOperator(":")

                val type =
                        parseType()

                return LetSignature(id.location + type.location, id, generaliseType(type))
            } else if (lexer.token == Token.LowerID || isOperator("=") || isOperator("|")) {
                val arguments =
                        mutableListOf<ID>()

                while (lexer.token == Token.LowerID) {
                    arguments.add(lexer.next().toID())
                }

                if (isOperator("|")) {
                    val guardedExpressions =
                            mutableListOf<Pair<Expression, Expression>>()

                    while (isOperator("|")) {
                        matchOperator("|")

                        val guard =
                                parseExpression()

                        matchOperator("=")

                        val expression =
                                parseExpression()

                        guardedExpressions.add(Pair(guard, expression))
                    }

                    return LetGuardDeclaration(id.location + locationFrom(guardedExpressions.map { it.second }), id, arguments, guardedExpressions)
                } else {
                    matchOperator("=")

                    val expression =
                            parseExpression()

                    return LetDeclaration(locationFrom(listOf(id, expression))!!, id, arguments, expression)
                }
            } else {
                TODO()
            }
        } else {
            throw syntaxError("Expected typealias, type or LowerID")
        }
    }


    fun parseTypeConstructor(): TypeConstructor {
        val upperID =
                matchToken(Token.UpperID, "Expected UpperID").toID()

        val arguments =
                mutableListOf<TType>()

        while (lexer.column > 2 && (
                        lexer.token == Token.LowerID ||
                                lexer.token == Token.UpperID ||
                                isOperator("(") ||
                                isOperator("()"))) {
            arguments.add(parseType())
        }

        return TypeConstructor(upperID.location + locationFrom(arguments), upperID, arguments)
    }


    fun parseExpression(): Expression =
            parseMultiplicative()


    fun parseMultiplicative(): Expression =
            parseBinaryOp(setOf("*", "/")) { parseAdditive() }

    fun parseAdditive(): Expression =
            parseBinaryOp(setOf("+", "-")) { parseRelationalOp() }

    fun parseRelationalOp(): Expression =
            parseBinaryOp(setOf("==", "!=", "<=", "<", ">=", ">")) { parseBooleanAnd() }

    fun parseBooleanAnd(): Expression =
            parseBinaryOp(setOf("&&")) { parseBooleanOr() }

    fun parseBooleanOr(): Expression =
            parseBinaryOp(setOf("||")) { parseLambda() }


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

    fun parseLambda(): Expression =
            if (isOperator("\\")) {
                val lambdaSymbol =
                        lexer.next()

                val arguments =
                        mutableListOf<ID>()

                arguments.add(matchToken(Token.LowerID, "lower ID").toID())

                while (lexer.token == Token.LowerID) {
                    arguments.add(matchToken(Token.LowerID, "lower ID").toID())
                }

                matchOperator("->")

                val expression =
                        parseExpression()

                LambdaExpression(lambdaSymbol.location + expression.location, arguments, expression)
            } else {
                parseIf()
            }


    fun parseIf(): Expression =
            if (lexer.token == Token.IF) {
                val ifSymbol =
                        lexer.next()

                val guardExpression =
                        parseExpression()

                matchToken(Token.THEN, "then")

                val thenExpression =
                        parseExpression()

                matchToken(Token.ELSE, "else")

                val elseExpression =
                        parseExpression()

                IfExpression(ifSymbol.location + elseExpression.location, guardExpression, thenExpression, elseExpression)
            } else {
                parseCall()
            }


    fun parseCall(): Expression {
        val operator =
                parseTerm()

        val operands =
                mutableListOf<Expression>()

        while (lexer.column > 2 && (
                        isOperator("(") ||
                                lexer.token == Token.ConstantInt ||
                                lexer.token == Token.ConstantString ||
                                isOperator("!") ||
                                lexer.token == Token.LowerID ||
                                lexer.token == Token.UpperID ||
                                isOperator("()")
                        )) {
            operands.add(parseTerm())
        }

        return if (operands.isEmpty())
            operator
        else
            CallExpression(operator.location + locationFrom(operands), operator, operands)
    }


    fun parseTerm(): Expression =
            when {
                lexer.token == Token.ConstantInt -> {
                    val token =
                            lexer.next()

                    ConstantInt(token.location, token.text.toInt())
                }

                lexer.token == Token.UpperID && lexer.text == "True" ->
                    True(lexer.next().location)

                lexer.token == Token.UpperID && lexer.text == "False" ->
                    False(lexer.next().location)

                lexer.token == Token.ConstantString -> {
                    val token =
                            lexer.next()

                    val text =
                            token.text
                                    .drop(1)
                                    .dropLast(1)
                                    .replace("\\\\", "\\")
                                    .replace("\\\"", "\"")

                    ConstantString(token.location, text)
                }

                isOperator("!") -> {
                    val not =
                            lexer.next()

                    val expression =
                            parseExpression()

                    NotExpression(not.location + expression.location, expression)
                }

                lexer.token == Token.LowerID -> {
                    val lowerID =
                            lexer.next()

                    IdReference(lowerID.location, lowerID.text)
                }

                isOperator("(") -> {
                    lexer.next()

                    val expression =
                            parseExpression()

                    matchOperator(")")

                    expression
                }

                isOperator("()") -> {
                    val unit =
                            lexer.next()

                    Unit(unit.location)
                }

                lexer.token == Token.UpperID -> {
                    val lowerID =
                            lexer.next()

                    ConstructorReference(lowerID.location, lowerID.text)
                }

                else ->
                    TODO(lexer.next().toString())
            }


    fun parseType(): TType {
        val bType =
                parseBType()

        return if (isOperator("->")) {
            matchOperator("->")

            val type =
                    parseType()

            TArrow(bType.location + type.location, bType, type)
        } else {
            bType
        }
    }

    fun parseBType(): TType {
        if (lexer.token == Token.UpperID) {
            val upperID =
                    lexer.next().toID()

            val arguments =
                    mutableListOf<TType>()

            while (lexer.column > 2 && (
                            lexer.token == Token.LowerID ||
                                    lexer.token == Token.UpperID ||
                                    isOperator("(") ||
                                    isOperator("()"))) {
                arguments.add(parseBType())
            }

            return TConstReference(upperID.location + locationFrom(arguments), upperID, arguments)
        } else {
            return parseCType()
        }
    }


    private fun parseCType(): TType {
        when {
            lexer.token == Token.LowerID -> {
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


    private fun isOperator(text: String): Boolean =
            lexer.token == Token.ConstantOperator && lexer.text == text


    private fun isOperator(texts: Set<String>): Boolean =
            lexer.token == Token.ConstantOperator && texts.contains(lexer.text)


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