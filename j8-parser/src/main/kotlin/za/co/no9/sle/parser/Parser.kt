package za.co.no9.sle.parser

import org.apache.commons.text.StringEscapeUtils
import za.co.no9.sle.*
import za.co.no9.sle.ast.typeless.*
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

        matchToken(Token.EXPORT, "export")

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
                            next().toID()

                    LetExport(lowerID.location, lowerID)
                }

                isToken(Token.UpperID) -> {
                    val upperID =
                            next().toID()

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
                            next()

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
                matchToken(Token.IMPORT, "import")

        val importURN =
                matchToken(Token.ImportURN, "Import URN").toID()

        var lastLocation =
                importURN.location

        val asName = if (isToken(Token.AS)) {
            skip()
            val result =
                    matchToken(Token.UpperID, "UpperID").toID()

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
                            next()

                    LetNamedDeclaration(lowerID.location, lowerID.toID())
                }

                isToken(Token.UpperID) -> {
                    val upperID =
                            next().toID()

                    if (isOperator("(")) {
                        next()
                        matchOperator("..")
                        val closingParenthesis =
                                matchOperator(")")

                        TypeNamedDeclaration(upperID.location + closingParenthesis.location, upperID, true)
                    } else
                        TypeNamedDeclaration(upperID.location, upperID, false)
                }

                isOperator("(") -> {
                    val openParen =
                            next()

                    val operator =
                            matchOperator()

                    val closeParen =
                            matchOperator(")")

                    OperatorNamedDeclaration(openParen.location + closeParen.location, operator.toID())
                }

                else ->
                    throw syntaxError("Expected UpperID, LowerID or '('")
            }


    fun parseDeclaration(): Declaration =
            when {
                isToken(Token.TYPE) -> {
                    val typeSymbol =
                            next()

                    val upperID =
                            matchToken(Token.UpperID, "UpperID").toID()

                    val arguments =
                            mutableListOf<ID>()

                    while (isToken(Token.LowerID)) {
                        arguments.add(next().toID())
                    }

                    matchOperator("=")

                    val typeConstructors =
                            mutableListOf<TypeConstructor>()

                    typeConstructors.add(parseTypeConstructor(typeSymbol.column))

                    while (isOperator("|")) {
                        matchOperator("|")
                        typeConstructors.add(parseTypeConstructor(typeSymbol.column))
                    }

                    TypeDeclaration(typeSymbol.location + locationFrom(typeConstructors), upperID, arguments, typeConstructors)
                }

                isToken(Token.TYPEALIAS) -> {
                    val typealiasSymbol =
                            next()

                    val upperID =
                            matchToken(Token.UpperID, "UpperID").toID()

                    matchOperator("=")

                    val type =
                            parseType(typealiasSymbol.column)

                    TypeAliasDeclaration(typealiasSymbol.location + type.location, upperID, type)
                }

                isFirstLetDeclaration() -> {
                    parseLetDeclaration()
                }

                else ->
                    throw syntaxError("Expected typealias, type, LowerID or '('")
            }


    fun isFirstLetDeclaration(): Boolean =
            isToken(Token.LowerID) || isOperator("(")


    fun parseLetDeclaration(): ComponentLetDeclaration {
        val id =
                parseValueDeclarationID()

        if (isOperator(":")) {
            matchOperator(":")

            val type =
                    parseType(id.column)

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


    fun parseValueDeclarationID(): ValueDeclarationID =
            when {
                isToken(Token.LowerID) -> {
                    val id =
                            next().toID()

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


    fun parseTypeConstructor(leftEdge: Int): TypeConstructor {
        val upperID =
                matchToken(Token.UpperID, "UpperID").toID()

        val arguments =
                mutableListOf<TType>()

        while (lexer.column > 2 && (
                        isToken(Token.LowerID) ||
                                isToken(Token.UpperID) ||
                                isOperator("("))) {
            arguments.add(parseTermType(leftEdge))
        }

        return TypeConstructor(upperID.location + locationFrom(arguments), upperID, arguments)
    }


    fun parseExpression(leftEdge: Int): Expression =
            parseLetExpression(leftEdge)


    fun parseLetExpression(leftEdge: Int): Expression =
            when {
                isToken(Token.LET) -> {
                    val letSymbol =
                            next()

                    val componentLetDeclarations =
                            mutableListOf<ComponentLetDeclaration>()

                    componentLetDeclarations.add(parseLetDeclaration())

                    while (isFirstLetDeclaration()) {
                        componentLetDeclarations.add(parseLetDeclaration())
                    }

                    matchToken(Token.IN, "in")

                    val expression =
                            parseLetExpression(leftEdge)

                    LetExpression(letSymbol.location + expression.location, componentLetDeclarations, expression)
                }

                else ->
                    parseCaseExpression(leftEdge)
            }


    fun parseCaseExpression(leftEdge: Int): Expression =
            when {
                isToken(Token.CASE) -> {
                    val caseSymbol =
                            next()

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
            setOf("(", ")", "|", "=", ",", "[", "]", "{", "}")


    fun parseBinaryOperators(leftEdge: Int): Expression {
        val left =
                parseLambda(leftEdge)

        return if (isOperatorExcluded(excludeOperators)) {
            val operator =
                    next()

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
                            next()

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
                            next()

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
                parseFieldProjection(leftEdge)

        val operands =
                mutableListOf<Expression>()

        while (lexer.column > leftEdge && isFirstTerm()) {
            operands.add(parseFieldProjection(leftEdge))
        }

        return if (operands.isEmpty())
            operator
        else
            CallExpression(operator.location + locationFrom(operands), operator, operands)
    }


    fun parseFieldProjection(leftEdge: Int): Expression {
        var result =
                parseTerm(leftEdge)

        while (isOperator(".")) {
            skip()

            val name =
                    matchToken(Token.LowerID, "Lower ID")

            result = FieldProjectionExpression(result.location + name.location, result, name.toID())
        }

        return result
    }


    fun parseTerm(leftEdge: Int): Expression =
            when {
                isToken(Token.ConstantInt) -> {
                    val token =
                            next()

                    ConstantInt(token.location, token.text.toInt())
                }

                isToken(Token.UpperID) && lexer.text == "True" ->
                    True(next().location)

                isToken(Token.UpperID) && lexer.text == "False" ->
                    False(next().location)

                isToken(Token.ConstantString) -> {
                    val constantStringSymbol =
                            next()

                    val text =
                            resolveStringEscape(constantStringSymbol.text
                                    .drop(1)
                                    .dropLast(1))

                    ConstantString(constantStringSymbol.location, text)
                }
                isToken(Token.ConstantChar) -> {
                    val constantCharSymbol =
                            next()

                    val text =
                            resolveStringEscape(constantCharSymbol.text
                                    .drop(1)
                                    .dropLast(1))

                    ConstantChar(constantCharSymbol.location, text[0])
                }

                isOperator("[]") -> {
                    val nilSymbol =
                            next()
                    ConstantList(nilSymbol.location, emptyList())
                }

                isOperator("[") -> {
                    val openSquare =
                            next()

                    val items =
                            mutableListOf<Expression>()

                    if (!isOperator("]")) {
                        items.add(parseExpression(leftEdge))

                        while (isOperator(",")) {
                            next()
                            items.add(parseExpression(leftEdge))
                        }
                    }

                    val endSquare =
                            matchOperator("]")

                    ConstantList(openSquare.location + endSquare.location, items)
                }

                isOperator("{") -> {
                    val openCurley =
                            next()

                    if (isOperator("}")) {
                        val closeCurley =
                                matchOperator("}")

                        ConstantRecord(openCurley.location + closeCurley.location, emptyList())
                    } else {
                        val first =
                                parseExpression(leftEdge)

                        if (isOperator("|")) {
                            skip()

                            val fields =
                                    mutableListOf<Pair<ID, Expression>>()

                            while(true) {
                                val nextName =
                                        matchToken(Token.LowerID, "Lower ID")

                                matchOperator("=")

                                val nextExpression =
                                        parseExpression(leftEdge)

                                fields.add(Pair(nextName.toID(), nextExpression))

                                if (isOperator(","))
                                    skip()
                                else
                                    break
                            }
                            val closeCurley =
                                    matchOperator("}")

                            UpdateRecordExpression(openCurley.location + closeCurley.location, first, fields)
                        } else {
                            val name =
                                    if (first is IdReference && first.name.qualifier == null)
                                        ID(first.location, first.name.name)
                                    else
                                        throw syntaxError("Expected a Lower ID")

                            matchOperator("=")
                            val expression =
                                    parseExpression(leftEdge)

                            val fields =
                                    mutableListOf(ConstantField(name.location + expression.location, name, expression))

                            while (isOperator(",")) {
                                skip()

                                val nextName =
                                        matchToken(Token.LowerID, "Lower ID")

                                matchOperator("=")

                                val nextExpression =
                                        parseExpression(leftEdge)

                                fields.add(ConstantField(nextName.location + nextExpression.location, nextName.toID(), nextExpression))
                            }
                            val closeCurley =
                                    matchOperator("}")

                            ConstantRecord(openCurley.location + closeCurley.location, fields)
                        }
                    }
                }

                isOperator("!") -> {
                    val bangSymbol =
                            next()

                    val expression =
                            parseExpression(leftEdge)

                    NotExpression(bangSymbol.location + expression.location, expression)
                }

                isToken(Token.LowerID) -> {
                    val lowerIDSymbol =
                            next()

                    IdReference(lowerIDSymbol.location, QualifiedID(lowerIDSymbol.location, null, lowerIDSymbol.text))
                }

                isOperator("(") -> {
                    val openParen =
                            next()

                    if (isOperator(")")) {
                        val closeParen =
                                next()

                        NestedExpressions(openParen.location + closeParen.location, emptyList())
                    } else if (isOperator() && !isOperator("\\") && !isOperator("(") && !isOperator("[") && !isOperator("!") && !isOperator("{")) {
                        val operator =
                                next()

                        val closeParen =
                                matchOperator(")")

                        IdReference(openParen.location + closeParen.location, QualifiedID(operator.location, null, operator.text))
                    } else {
                        val expressions =
                                mutableListOf<Expression>()

                        expressions.add(parseExpression(leftEdge))

                        while (isOperator(",")) {
                            skip()
                            expressions.add(parseExpression(leftEdge))
                        }

                        val closeParen =
                                matchOperator(")")

                        NestedExpressions(openParen.location + closeParen.location, expressions)
                    }
                }

                isToken(Token.UpperID) -> {
                    val upperID =
                            next()

                    if (isOperator(".")) {
                        skip()

                        when {
                            isToken(Token.UpperID) -> {
                                val name =
                                        next()

                                ConstructorReference(upperID.location + name.location, QualifiedID(upperID.location + name.location, upperID.text, name.text))
                            }

                            isToken(Token.LowerID) -> {
                                val name =
                                        next()

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
                    TODO(next().toString())
            }

    fun isFirstTerm(): Boolean =
            isOperator("(") ||
                    isToken(Token.ConstantInt) ||
                    isToken(Token.ConstantString) ||
                    isToken(Token.ConstantChar) ||
                    isOperator("!") ||
                    isToken(Token.LowerID) ||
                    isToken(Token.UpperID) ||
                    isOperator("[") ||
                    isOperator("[]") ||
                    isOperator("{")


    fun parseType(leftEdge: Int): TType {
        val bType =
                parseADTType(leftEdge)

        return if (isOperator("->")) {
            matchOperator("->")

            val type =
                    parseType(leftEdge)

            TArrow(bType.location + type.location, bType, type)
        } else {
            bType
        }
    }


    fun parseADTType(leftEdge: Int): TType =
            when {
                isToken(Token.UpperID) -> {
                    val qualifiedUpperID =
                            parseQualifiedUpperID()

                    val arguments =
                            mutableListOf<TType>()

                    while (lexer.column > leftEdge && isFirstADTType()) {
                        arguments.add(parseTermType(leftEdge))
                    }

                    TTypeReference(qualifiedUpperID.location + locationFrom(arguments), qualifiedUpperID, arguments)
                }

                else ->
                    parseTermType(leftEdge)
            }


    fun parseQualifiedUpperID(): QualifiedID =
            when {
                isToken(Token.UpperID) -> {
                    val upperID =
                            next().toID()

                    if (isOperator(".")) {
                        skip()
                        val otherUpperID =
                                next().toID()
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


    fun parseTermType(leftEdge: Int): TType =
            when {
                isToken(Token.LowerID) -> {
                    val lowerID =
                            next()

                    TVarReference(lowerID.location, lowerID.text)
                }

                isToken(Token.UpperID) -> {
                    val qualifiedUpperID =
                            parseQualifiedUpperID()

                    TTypeReference(qualifiedUpperID.location, qualifiedUpperID, emptyList())
                }

                isOperator("(") -> {
                    val openParenSymbol =
                            matchOperator("(")

                    val types =
                            mutableListOf<TType>()

                    if (!isOperator(")")) {
                        types.add(parseType(leftEdge))

                        while (isOperator(",")) {
                            skip()
                            types.add(parseType(leftEdge))
                        }
                    }
                    val closeParenSymbol =
                            matchOperator(")")

                    TNTuple(openParenSymbol.location + closeParenSymbol.location, types)
                }

                isOperator("{") -> {
                    val openCurley =
                            matchOperator("{")

                    val fields =
                            mutableListOf<Pair<ID, TType>>()

                    if (isToken(Token.LowerID)) {
                        while (true) {
                            val lowerID =
                                    matchToken(Token.LowerID, "Lower ID").toID()

                            matchOperator(":")

                            val type =
                                    parseType(leftEdge)

                            fields.add(Pair(lowerID, type))

                            if (isOperator(",")) {
                                skip()
                            } else {
                                break
                            }
                        }
                    }

                    val closeCurley =
                            matchOperator("}")

                    TRecord(openCurley.location + closeCurley.location, fields)
                }

                else -> {
                    throw syntaxError("Expected lower ID, upper ID, '(' or '{'")
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
                    parseConsOperatorPattern()
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


    fun parseConsOperatorPattern(): Pattern {
        val pattern =
                parseTermPattern()

        return if (isOperator("::")) {
            val patterns =
                    mutableListOf<Pattern>()

            patterns.add(pattern)
            while (isOperator("::")) {
                next()
                patterns.add(parseTermPattern())
            }

            patterns.dropLast(1).foldRight(patterns.last()) { a, b ->
                ConsOperatorPattern(a.location + b.location, a, b)
            }
        } else {
            pattern
        }
    }


    fun parseTermPattern(): Pattern =
            when {
                isToken(Token.ConstantInt) -> {
                    val constantIntSymbol =
                            next()

                    ConstantIntPattern(constantIntSymbol.location, constantIntSymbol.text.toInt())
                }

                isToken(Token.UpperID) && lexer.text == "True" -> {
                    val trueSymbol =
                            next()

                    ConstantBoolPattern(trueSymbol.location, true)
                }

                isToken(Token.UpperID) && lexer.text == "False" -> {
                    val trueSymbol =
                            next()

                    ConstantBoolPattern(trueSymbol.location, false)
                }

                isToken(Token.ConstantString) -> {
                    val constantStringSymbol =
                            next()

                    val text =
                            resolveStringEscape(constantStringSymbol.text
                                    .drop(1)
                                    .dropLast(1))

                    ConstantStringPattern(constantStringSymbol.location, text)
                }

                isToken(Token.ConstantChar) -> {
                    val constantCharSymbol =
                            next()

                    val text =
                            resolveStringEscape(constantCharSymbol.text
                                    .drop(1)
                                    .dropLast(1))

                    ConstantCharPattern(constantCharSymbol.location, text[0])
                }

                isOperator("[]") -> {
                    val nilSymbol =
                            next()
                    ConstantListPattern(nilSymbol.location, emptyList())
                }

                isOperator("[") -> {
                    val openSquare =
                            next()

                    val items =
                            mutableListOf<Pattern>()

                    if (!isOperator("]")) {
                        items.add(parsePattern())

                        while (isOperator(",")) {
                            skip()
                            items.add(parsePattern())
                        }
                    }

                    val endSquare =
                            matchOperator("]")

                    ConstantListPattern(openSquare.location + endSquare.location, items)
                }

                isOperator("(") -> {
                    val openParenSymbol =
                            next()

                    val patterns =
                            mutableListOf<Pattern>()

                    if (!isOperator(")")) {
                        patterns.add(parsePattern())

                        while (isOperator(",")) {
                            skip()
                            patterns.add(parsePattern())

                        }
                    }
                    val closeParenSymbol =
                            matchOperator(")")

                    ConstantNTuplePattern(openParenSymbol.location + closeParenSymbol.location, patterns)
                }

                isToken(Token.LowerID) -> {
                    val lowerIDSymbol =
                            next()

                    IdReferencePattern(lowerIDSymbol.location, lowerIDSymbol.text)
                }

                isToken(Token.Unknown) -> {
                    val unknownSymbol =
                            next()

                    IgnorePattern(unknownSymbol.location)
                }

                isOperator("{") -> {
                    val openParen =
                            next()

                    val values =
                            mutableListOf<Pair<ID, Pattern?>>()

                    if (isToken(Token.LowerID)) {
                        while (true) {
                            val name =
                                    matchToken(Token.LowerID, "Expected Lower ID").toID()

                            if (isOperator("=")) {
                                matchOperator("=")

                                val pattern =
                                        parsePattern()

                                values.add(Pair(name, pattern))
                            } else {
                                values.add(Pair(name, null))
                            }

                            if (isOperator(",")) {
                                skip()
                            } else {
                                break
                            }
                        }
                    }

                    val closeParen =
                            matchOperator("}")

                    RecordPattern(openParen.location + closeParen.location, values)
                }

                else ->
                    throw syntaxError("Expected constant int, constant string, lower ID, upperID, True, False, '(' or '_'")
            }


    fun isFirstPattern(): Boolean =
            isToken(Token.ConstantInt) ||
                    isToken(Token.ConstantString) ||
                    isToken(Token.ConstantChar) ||
                    isToken(Token.UpperID) ||
                    isToken(Token.LowerID) ||
                    isOperator("(") ||
                    isOperator("[") ||
                    isOperator("[]") ||
                    isOperator("{") ||
                    isToken(Token.Unknown)


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
            return next()
        } else {
            throw syntaxError("Expected '$text'")
        }
    }


    private fun matchOperator(): Symbol {
        if (isOperator()) {
            return next()
        } else {
            throw syntaxError("Expected an operator")
        }
    }


    private fun matchToken(token: Token, text: String): Symbol {
        if (lexer.token == token) {
            return next()
        } else {
            throw syntaxError("Expected $text")
        }
    }


    private fun skip() =
            lexer.skip()


    private fun next() =
            lexer.next()


    private fun syntaxError(text: String): Exception {
        errors += SyntaxError(lexer.currentSymbol.location, text)
        return Exception("Syntax Error")
    }
}


private fun locationFrom(nodes: List<Node>): Location? =
        location(nodes.map { it.location })


private fun Symbol.toID(): ID =
        ID(this.location, this.text)


private fun resolveStringEscape(input: String): String =
        StringEscapeUtils.unescapeJava(input)
