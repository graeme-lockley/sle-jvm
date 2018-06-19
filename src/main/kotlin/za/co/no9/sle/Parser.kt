package za.co.no9.sle

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTreeWalker
import za.co.no9.sle.ast.*
import za.co.no9.sle.ast.Position as ASTPosition


typealias ParseResult =
        Either<SyntaxError, Result>


private class ParserErrorListener : BaseErrorListener() {
    var syntaxError: SyntaxError? = null

    override fun syntaxError(recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int, charPositionInLine: Int, msg: String?, e: RecognitionException?) {
        syntaxError = SyntaxError(Position(line, charPositionInLine), msg ?: "Syntax error")
    }
}


class Result(val parser: ParserParser, val node: ParserRuleContext) {
    val stringTree: String
        get() = node.toStringTree(parser)

    val constraints: List<String>
        get() = emptyList()


    fun parserToAST(): ParserToAST {
        val walker =
                ParseTreeWalker()

        val parserToAST =
                ParserToAST()

        walker.walk(parserToAST, node)

        return parserToAST
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
            value(Result(parser, parseTree))
        else ->
            error(syntaxError)
    }
}


fun parseTextAsFactor(text: String): ParseResult {
    val lexer = ParserLexer(CharStreams.fromString(text))
    val tokens = CommonTokenStream(lexer)

    val parser = ParserParser(tokens)
    val errorListener = ParserErrorListener()

    parser.removeErrorListeners()
    parser.addErrorListener(errorListener)

    val parseTree = parser.factor()

    val syntaxError = errorListener.syntaxError
    return when (syntaxError) {
        null ->
            value(Result(parser, parseTree))
        else ->
            error(syntaxError)
    }
}


class ParserToAST : ParserBaseListener() {
    private var expressionStack =
            emptyList<Expression>()


    fun popExpression(): Expression {
        val result =
                expressionStack.last()

        expressionStack = expressionStack.dropLast(1)

        return result
    }


    private fun pushExpression(expression: Expression) {
        expressionStack = expressionStack.plus(expression)
    }


    override fun exitTrueExpression(ctx: ParserParser.TrueExpressionContext?) {
        pushExpression(True(position(ctx!!)))
    }


    override fun exitFalseExpression(ctx: ParserParser.FalseExpressionContext?) {
        pushExpression(False(position(ctx!!)))
    }

    override fun exitConstantIntExpression(ctx: ParserParser.ConstantIntExpressionContext?) {
        pushExpression(ConstantInt(position(ctx!!), ctx.text.toInt()))
    }

    override fun exitConstantStringExpression(ctx: ParserParser.ConstantStringExpressionContext?) {
        val text =
                ctx!!.text
                        .drop(1)
                        .dropLast(1)
                        .replace("\\\\", "\\")
                        .replace("\\\"", "\"")

        pushExpression(ConstantString(position(ctx), text))
    }
}


private fun position(ctx: ParserRuleContext) =
        ASTPosition(ctx.start.line, ctx.start.charPositionInLine)