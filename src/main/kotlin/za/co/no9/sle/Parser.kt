package za.co.no9.sle

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.antlr.v4.runtime.tree.TerminalNode
import za.co.no9.sle.ast.*
import za.co.no9.sle.ast.pass1.*
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


class ParserToAST : ParserBaseListener() {
    private var expressionStack =
            emptyList<Expression>()

    var module: Module? =
            null
        private set


    private var declarations =
            emptyList<Declaration>()


    fun popExpression(): Expression {
        val result =
                expressionStack.last()

        expressionStack = expressionStack.dropLast(1)

        return result
    }


    private fun pushExpression(expression: Expression) {
        expressionStack += expression
    }


    private fun addDeclaration(declaration: Declaration) {
        declarations += declaration
    }


    private fun popDeclarations(): List<Declaration> {
        val result =
                declarations

        declarations = emptyList()

        return result
    }


    override fun exitTrueExpression(ctx: ParserParser.TrueExpressionContext?) =
            pushExpression(True(ctx!!.location()))

    override fun exitFalseExpression(ctx: ParserParser.FalseExpressionContext?) =
            pushExpression(False(ctx!!.location()))

    override fun exitConstantIntExpression(ctx: ParserParser.ConstantIntExpressionContext?) =
            pushExpression(ConstantInt(ctx!!.location(), ctx.text.toInt()))

    override fun exitConstantStringExpression(ctx: ParserParser.ConstantStringExpressionContext?) {
        val text =
                ctx!!.text
                        .drop(1)
                        .dropLast(1)
                        .replace("\\\\", "\\")
                        .replace("\\\"", "\"")

        pushExpression(ConstantString(ctx.location(), text))
    }

    override fun exitNotExpression(ctx: ParserParser.NotExpressionContext?) =
            pushExpression(NotExpression(ctx!!.location(), popExpression()))

    override fun exitLowerIDExpression(ctx: ParserParser.LowerIDExpressionContext?) =
            pushExpression(IdReference(ctx!!.location(), ctx.text))

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

        pushExpression(LambdaExpression(ctx!!.location(), ctx.LowerID().toList().map { IdReference(it.location(), it.text) }, expression))
    }

    override fun exitBooleanOrExpression(ctx: ParserParser.BooleanOrExpressionContext?) =
            translateBinaryOperatorExpression(ctx!!, ctx.op)

    override fun exitBooleanAndExpression(ctx: ParserParser.BooleanAndExpressionContext?) =
            translateBinaryOperatorExpression(ctx!!, ctx.op)

    override fun exitRelationalOpExpression(ctx: ParserParser.RelationalOpExpressionContext?) =
            translateBinaryOperatorExpression(ctx!!, ctx.op)

    override fun exitAdditiveExpression(ctx: ParserParser.AdditiveExpressionContext?) =
            translateBinaryOperatorExpression(ctx!!, ctx.op)

    override fun exitMultiplicativeExpression(ctx: ParserParser.MultiplicativeExpressionContext?) =
            translateBinaryOperatorExpression(ctx!!, ctx.op)

    private fun translateBinaryOperatorExpression(ctx: ParserParser.ExpressionContext, op: Token) {
        val right =
                popExpression()
        val left =
                popExpression()

        pushExpression(BinaryOpExpression(ctx.location(), left, IdReference(op.location(), op.text), right))
    }

    override fun exitCallExpression(ctx: ParserParser.CallExpressionContext?) {
        val numberOfOperands = ctx!!.childCount - 1

        if (numberOfOperands > 0) {
            val operands =
                    mutableListOf<Expression>()

            for (lp in 1..numberOfOperands) {
                operands.add(popExpression())
            }

            val operator =
                    popExpression()

            pushExpression(CallExpression(ctx.location(), operator, operands.toList().asReversed()))
        }
    }

    override fun exitLetDeclaration(ctx: ParserParser.LetDeclarationContext?) {
        val names =
                ctx!!.LowerID().toList().map { IdReference(it.location(), it.text) }

        val expression =
                popExpression()

        addDeclaration(LetDeclaration(ctx.location(), names[0], names.drop(1), expression))
    }


    override fun exitModule(ctx: ParserParser.ModuleContext?) {
        module = Module(ctx!!.location(), popDeclarations())
    }
}


private fun ParserRuleContext.location(): Location {
    fun ParserRuleContext.startPosition() =
            ASTPosition(this.start.line, this.start.charPositionInLine)

    fun ParserRuleContext.endPosition() =
            ASTPosition(this.stop.line, this.stop.charPositionInLine + this.stop.text.length - 1)

    return Location(this.startPosition(), this.endPosition())
}

private fun TerminalNode.location(): Location =
        this.symbol.location()

private fun Token.location(): Location =
        Location(ASTPosition(this.line, this.charPositionInLine), ASTPosition(this.line, this.charPositionInLine + this.text.length - 1))
