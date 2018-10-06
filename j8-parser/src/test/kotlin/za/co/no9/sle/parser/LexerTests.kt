package za.co.no9.sle.parser

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

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
