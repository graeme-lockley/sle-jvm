package za.co.no9.sle.parser

import za.co.no9.sle.Location
import za.co.no9.sle.Position


enum class Token {
    EOF,

    ERROR,

    ConstantInt,
    ConstantOperator,
    ConstantString,

    LowerID,
    UpperID,

    CASE,
    ELSE,
    IF,
    OF,
    THEN,
    TYPE,
    TYPEALIAS
}

val keywords =
        mapOf(
                Pair("case", Token.CASE),
                Pair("else", Token.ELSE),
                Pair("if", Token.IF),
                Pair("of", Token.OF),
                Pair("then", Token.THEN),
                Pair("type", Token.TYPE),
                Pair("typealias", Token.TYPEALIAS)
        )

val operatorCharacters =
        setOf('-', '=', '|', '(', ')', '*', '/', '=', '!', '<', '>', '&', '\\', '+', ':')


data class Symbol(val token: Token, val location: Location, val text: String) {
    val column: Int
        get() = location.start.column
}


class Lexer(val input: String) {
    var currentSymbol =
            Symbol(Token.EOF, Location(Position(0, 0), Position(0, 0)), "")
        private set

    val token: Token
        get() = this.currentSymbol.token

    val text: String
        get() = this.currentSymbol.text

    val column: Int
        get() = this.currentSymbol.location.start.column


    private val inputLength =
            input.length

    private var currentIndex =
            -1

    private var currentLine =
            1

    private var currentColumn =
            0

    private var currentCh =
            ' '

    private var nextCh =
            ' '

    init {
        skip()
    }

    fun next(): Symbol {
        val current =
                currentSymbol

        skip()

        return current
    }


    fun skip() {
        try {
            nextCharacter()

            while (currentCh.isWhitespace()) {
                nextCharacter()
            }

            when {
                currentCh in 'a'..'z' -> {
                    val startPosition =
                            position()

                    val startIndex =
                            currentIndex

                    while (nextCh.isLetterOrDigit() || nextCh == '_') {
                        nextCharacter()
                    }
                    while (nextCh == '\'') {
                        nextCharacter()
                    }

                    val endPosition =
                            position()

                    val endIndex =
                            currentIndex

                    val text =
                            input.substring(startIndex, endIndex + 1)

                    currentSymbol =
                            Symbol(keywords.getOrDefault(text, Token.LowerID), Location(startPosition, endPosition), text)
                }

                currentCh in 'A'..'Z' -> {
                    val startPosition =
                            position()

                    val startIndex =
                            currentIndex

                    while (nextCh.isLetterOrDigit() || nextCh == '_') {
                        nextCharacter()
                    }
                    while (nextCh == '\'') {
                        nextCharacter()
                    }

                    val endPosition =
                            position()

                    val endIndex =
                            currentIndex

                    val text =
                            input.substring(startIndex, endIndex + 1)

                    currentSymbol =
                            Symbol(Token.UpperID, Location(startPosition, endPosition), text)
                }

                currentCh in '0'..'9' -> {
                    val startPosition =
                            position()

                    val startIndex =
                            currentIndex

                    while (nextCh.isDigit()) {
                        nextCharacter()
                    }
                    val endPosition =
                            position()

                    val endIndex =
                            currentIndex

                    val text =
                            input.substring(startIndex, endIndex + 1)

                    currentSymbol =
                            Symbol(Token.ConstantInt, Location(startPosition, endPosition), text)
                }

                currentCh == '"' -> {
                    val startPosition =
                            position()

                    val startIndex =
                            currentIndex

                    nextCharacter()
                    while (true) {
                        if (currentCh == '"') {
                            break
                        } else if (currentCh == '\\') {
                            nextCharacter()
                            nextCharacter()
                        } else {
                            nextCharacter()
                        }
                    }

                    val endPosition =
                            position()

                    val endIndex =
                            currentIndex

                    val text =
                            input.substring(startIndex, endIndex + 1)

                    currentSymbol =
                            Symbol(Token.ConstantString, Location(startPosition, endPosition), text)
                }

                operatorCharacters.contains(currentCh) -> {
                    val startPosition =
                            position()

                    val startIndex =
                            currentIndex

                    while (operatorCharacters.contains(nextCh)) {
                        nextCharacter()
                    }
                    val endPosition =
                            position()

                    val endIndex =
                            currentIndex

                    val text =
                            input.substring(startIndex, endIndex + 1)

                    currentSymbol =
                            Symbol(Token.ConstantOperator, Location(startPosition, endPosition), text)
                }
            }
        } catch (e: StringIndexOutOfBoundsException) {
            currentSymbol = Symbol(Token.EOF, location(), "")
        }
    }


    private fun nextCharacter() {
        if (currentIndex < inputLength) {
            currentIndex += 1
            currentCh = input[currentIndex]
            nextCh = if (currentIndex + 1 < inputLength) input[currentIndex + 1] else ' '

            if (currentCh == '\n') {
                currentColumn = 0
                currentLine += 1
            } else if (currentCh != '\r'){
                currentColumn += 1
            }
        }
    }

    fun location(): Location =
            Location(position())


    private fun position(): Position =
            Position(currentLine, currentColumn)
}