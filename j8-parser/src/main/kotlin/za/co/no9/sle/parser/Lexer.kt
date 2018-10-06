package za.co.no9.sle.parser

import za.co.no9.sle.Location
import za.co.no9.sle.Position


enum class Token {
    EOF,

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

data class Symbol(val token: Token, val location: Location, val text: String)


class Lexer(val input: String) {
    var currentSymbol =
            Symbol(Token.EOF, Location(Position(0, 0), Position(0, 0)), "")
        private set

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

    fun skip() {
        try {
            next()

            while (currentCh.isWhitespace()) {
                next()
            }

            when {
                currentCh in 'A'..'Z' -> {
                    val startPosition =
                            position()

                    val startIndex =
                            currentIndex

                    while (nextCh.isLetterOrDigit() || nextCh == '_') {
                        next()
                    }
                    while (nextCh == '\'') {
                        next()
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

                currentCh in 'a'..'z' -> {
                    val startPosition =
                            position()

                    val startIndex =
                            currentIndex

                    while (nextCh.isLetterOrDigit() || nextCh == '_') {
                        next()
                    }
                    while (nextCh == '\'') {
                        next()
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
            }
        } catch (e: StringIndexOutOfBoundsException) {
            currentSymbol = Symbol(Token.EOF, location(), "")
        }
    }


    private fun next() {
        if (currentIndex < inputLength) {
            currentIndex += 1
            currentCh = input[currentIndex]
            nextCh = if (currentIndex + 1 < inputLength) input[currentIndex + 1] else ' '

            if (currentCh == '\n') {
                currentColumn = 1
                currentLine += 1
            } else {
                currentColumn += 1
            }
        }
    }

    private fun location(): Location =
            Location(position())


    private fun position(): Position =
            Position(currentLine, currentColumn)
}