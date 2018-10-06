package za.co.no9.sle.parser

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.Location
import za.co.no9.sle.Position

class LexerTests : StringSpec({
    "hello -> [LowerID]" {
        Lexer("hello").tokens()
                .shouldBe(listOf(Token.LowerID))

        Lexer("hello").tokenTexts()
                .shouldBe(listOf("hello"))
    }

    "hello World -> [LowerID, UpperID]" {
        Lexer("hello World").tokens()
                .shouldBe(listOf(Token.LowerID, Token.UpperID))

        Lexer("hello World").tokenTexts()
                .shouldBe(listOf("hello", "World"))
    }

    "case else if of then type typealias  -> [CASE, ELSE, IF, OF, THEN, TYPE, TYPEALIAS]" {
        Lexer("case else if of then type typealias").tokens()
                .shouldBe(listOf(Token.CASE, Token.ELSE, Token.IF, Token.OF, Token.THEN, Token.TYPE, Token.TYPEALIAS))
    }

    "123 -> [ConstantInt]" {
        Lexer("123").tokens()
                .shouldBe(listOf(Token.ConstantInt))

        Lexer("123").tokenTexts()
                .shouldBe(listOf("123"))
    }

    "\"hello\" -> [ConstantString]" {
        Lexer("\"hello\"").tokens()
                .shouldBe(listOf(Token.ConstantString))

        Lexer("\"hello\"").tokenTexts()
                .shouldBe(listOf("\"hello\""))
    }

    "() ! != == <= < -> -> [ConstantOperator, ..., ConstantOperator]" {
        Lexer("() ! != == <= < ->").tokens()
                .shouldBe(listOf(Token.ConstantOperator, Token.ConstantOperator, Token.ConstantOperator, Token.ConstantOperator, Token.ConstantOperator, Token.ConstantOperator, Token.ConstantOperator))

        Lexer("() ! != == <= < ->").tokenTexts()
                .shouldBe(listOf("()", "!", "!=", "==", "<=", "<", "->"))
    }

    "\\a -> [ConstantOperator, LowerID]" {
        Lexer("\\a").tokens()
                .shouldBe(listOf(Token.ConstantOperator, Token.LowerID))

        Lexer("\\a").tokenTexts()
                .shouldBe(listOf("\\", "a"))
    }

    "\"typealias type\" should have the correct locations" {
        val symbols =
                Lexer("typealias type").symbols()

        symbols[0].location
                .shouldBe(Location(Position(1, 1), Position(1, 9)))

        symbols[1].location
                .shouldBe(Location(Position(1, 11), Position(1, 14)))
    }
})


fun Lexer.symbols(): List<Symbol> {
    var symbols =
            emptyList<Symbol>()

    do {
        symbols += this.currentSymbol
        this.skip()
    } while (this.currentSymbol.token != Token.EOF)

    return symbols
}

fun Lexer.tokens() =
        this.symbols().map { it.token }

fun Lexer.tokenTexts() =
        this.symbols().map { it.text }
