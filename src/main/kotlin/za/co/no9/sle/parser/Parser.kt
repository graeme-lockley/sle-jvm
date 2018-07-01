package za.co.no9.sle.parser

import org.antlr.v4.runtime.*
import za.co.no9.sle.*


typealias ParseResult =
        Either<SyntaxError, Result>


class Result(private val parser: ParserParser, val node: ParserRuleContext) {
    val stringTree: String
        get() = node.toStringTree(parser)
}


fun parseText(text: String): ParseResult =
        parse({ parser -> parser.module() }, text)


fun parseTextAsExpression(text: String): ParseResult =
        parse({ parser -> parser.expression() }, text)


private fun parse(production: (ParserParser) -> ParserRuleContext, text: String): ParseResult {
    val lexer = ParserLexer(CharStreams.fromString(text))
    val tokens = CommonTokenStream(lexer)

    val parser = ParserParser(tokens)
    val errorListener = ParserErrorListener()

    parser.removeErrorListeners()
    parser.addErrorListener(errorListener)

    val parseTree = production(parser)

    val syntaxError = errorListener.syntaxError
    return when (syntaxError) {
        null ->
            value(Result(parser, parseTree))
        else ->
            error(syntaxError)
    }
}


private class ParserErrorListener : BaseErrorListener() {
    var syntaxError: SyntaxError? = null

    override fun syntaxError(recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int, charPositionInLine: Int, msg: String?, e: RecognitionException?) {
        syntaxError = SyntaxError(Location(Position(line, charPositionInLine)), msg
                ?: "Syntax error")
    }
}


