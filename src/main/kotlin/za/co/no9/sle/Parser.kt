package za.co.no9.sle

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream


class ParseState(val parser: ParserParser) {
    private val parseTree = parser.module()

    val stringTree: String
        get() = parseTree.toStringTree(parser)
}


fun parseText(text: String): ParseState {
    val lexer = ParserLexer(CharStreams.fromString(text))
    val tokens = CommonTokenStream(lexer)

    return ParseState(ParserParser(tokens))
}