package za.co.no9.sle

import org.antlr.v4.runtime.*


typealias ParseResult =
        Either<SyntaxError, String>


private class ParserErrorListener : BaseErrorListener() {
    var syntaxError: SyntaxError? = null

    override fun syntaxError(recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int, charPositionInLine: Int, msg: String?, e: RecognitionException?) {
        syntaxError = SyntaxError(Position(line, charPositionInLine), msg ?: "Syntax error")
    }
}


fun parseText(text: String): ParseResult {
    val lexer = ParserLexer(CharStreams.fromString(text))
    val tokens = CommonTokenStream(lexer)

    val parser = ParserParser(tokens)
    val errorListener = ParserErrorListener()

    parser.removeErrorListeners()
    parser.addErrorListener(errorListener)

    val parseTree = parser.module()

    val syntaxError = errorListener.syntaxError
    return when (syntaxError) {
        null ->
            value(parseTree.toStringTree(parser))
        else ->
            error(syntaxError)
    }
}