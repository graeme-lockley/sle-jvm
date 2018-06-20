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


fun parseTextAsExpression(text: String): ParseResult {
    val lexer = ParserLexer(CharStreams.fromString(text))
    val tokens = CommonTokenStream(lexer)

    val parser = ParserParser(tokens)
    val errorListener = ParserErrorListener()

    parser.removeErrorListeners()
    parser.addErrorListener(errorListener)

    val parseTree = parser.expression()

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
        pushExpression(True(ctx!!.location()))
    }


    override fun exitFalseExpression(ctx: ParserParser.FalseExpressionContext?) {
        pushExpression(False(ctx!!.location()))
    }

    override fun exitConstantIntExpression(ctx: ParserParser.ConstantIntExpressionContext?) {
        pushExpression(ConstantInt(ctx!!.location(), ctx.text.toInt()))
    }

    override fun exitConstantStringExpression(ctx: ParserParser.ConstantStringExpressionContext?) {
        val text =
                ctx!!.text
                        .drop(1)
                        .dropLast(1)
                        .replace("\\\\", "\\")
                        .replace("\\\"", "\"")

        pushExpression(ConstantString(ctx.location(), text))
    }

    override fun exitNotExpression(ctx: ParserParser.NotExpressionContext?) {
        pushExpression(NotExpression(ctx!!.location(), popExpression()))
    }

    override fun exitLowerIDExpression(ctx: ParserParser.LowerIDExpressionContext?) {
        pushExpression(IdReference(ctx!!.location(), ctx.text))
    }

    override fun exitIfExpression(ctx: ParserParser.IfExpressionContext?) {
        val elseExpression =
                popExpression()

        val thenExpression =
                popExpression()

        val guardExpression =
                popExpression()

        pushExpression(IfExpression(ctx!!.location(), guardExpression, thenExpression, elseExpression))
    }

    override fun exitLambdaExpression(ctx: ParserParser.LambdaExpressionContext?) {
        val expression =
                popExpression()

        pushExpression(LambdaExpression(ctx!!.location(), ctx.LowerID().toList().map { it.text }, expression))
    }
}


private fun ParserRuleContext.startPosition() =
        ASTPosition(this.start.line, this.start.charPositionInLine)

private fun ParserRuleContext.endPosition() =
        ASTPosition(this.stop.line, this.stop.charPositionInLine + this.stop.text.length - 1)

private fun ParserRuleContext.location() =
        Location(this.startPosition(), this.endPosition())