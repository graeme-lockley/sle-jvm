package za.co.no9.sle.parseTreeToASTTranslator

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.antlr.v4.runtime.tree.TerminalNode
import za.co.no9.sle.*
import za.co.no9.sle.ast.typeless.*
import za.co.no9.sle.ast.typeless.Unit
import za.co.no9.sle.parser.parseModule


fun parse(text: String): Either<Errors, Module> {
    fun parseTreeToAST(node: ParserRuleContext): Module =
            walkParseTree(node).module!!

    return parseModule(text).map { parseTreeToAST(it.node) }
}


private fun walkParseTree(node: ParserRuleContext): ParserToAST {
    val walker =
            ParseTreeWalker()

    val parserToAST =
            ParserToAST()

    walker.walk(parserToAST, node)

    return parserToAST
}


private class ParserToAST : ParserBaseListener() {
    private var expressionStack =
            emptyList<Expression>()

    private var typeStack =
            emptyList<TType>()


    var module: Module? =
            null
        private set


    private var declarations =
            emptyList<Declaration>()


    private var schema: TSchema? =
            null


    private var typeParameters =
            emptyList<TypeParameter>()


    fun popExpression(): Expression {
        val result =
                expressionStack.last()

        expressionStack = expressionStack.dropLast(1)

        return result
    }


    private fun pushExpression(expression: Expression) {
        expressionStack += expression
    }


    fun popType(): TType? =
            when {
                typeStack.isEmpty() ->
                    null

                else -> {
                    val result =
                            typeStack.last()

                    typeStack = typeStack.dropLast(1)

                    result
                }
            }

    private fun pushType(type: TType) {
        typeStack += type
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

        pushExpression(LambdaExpression(ctx!!.location(), ctx.LowerID().toList().map { ID(it.location(), it.text) }, expression))
    }

    override fun exitUnitValueExpression(ctx: ParserParser.UnitValueExpressionContext?) {
        pushExpression(Unit(ctx!!.location()))
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

        pushExpression(BinaryOpExpression(ctx.location(), left, ID(op.location(), op.text), right))
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
                ctx!!.LowerID().toList().map { ID(it.location(), it.text) }

        val expression =
                popExpression()

        addDeclaration(LetDeclaration(ctx.location(), names[0], names.drop(1), schema, expression))
        schema = null
    }


    override fun exitTypeAliasDeclaration(ctx: ParserParser.TypeAliasDeclarationContext?) {
        addDeclaration(TypeAliasDeclaration(ctx!!.location(), ID(ctx.UpperID().location(), ctx.UpperID().text), schema!!))
        schema = null
    }


    override fun exitModule(ctx: ParserParser.ModuleContext?) {
        module = Module(ctx!!.location(), popDeclarations())
    }


    override fun exitSchema(ctx: ParserParser.SchemaContext?) {
        schema = TSchema(ctx!!.location(), typeParameters.toList(), popType()!!)
        typeParameters = emptyList()
    }


    override fun exitTypeParameter(ctx: ParserParser.TypeParameterContext?) {
        val type =
                if (ctx!!.type() == null) {
                    null
                } else {
                    popType()
                }

        typeParameters += TypeParameter(ctx.location(), ID(ctx.UpperID().location(), ctx.UpperID().text), type)
    }


    override fun exitUnitType(ctx: ParserParser.UnitTypeContext?) {
        pushType(TUnit(ctx!!.location()))
    }


    override fun exitUpperIDType(ctx: ParserParser.UpperIDTypeContext?) {
        pushType(TIdReference(ctx!!.location(), ctx.text))
    }


    override fun exitArrowType(ctx: ParserParser.ArrowTypeContext?) {
        val range =
                popType()!!

        val domain =
                popType()!!

        pushType(TArrow(ctx!!.location(), domain, range))
    }
}


private fun ParserRuleContext.location(): Location {
    fun ParserRuleContext.startPosition() =
            Position(this.start.line, this.start.charPositionInLine)

    fun ParserRuleContext.endPosition() =
            Position(this.stop.line, this.stop.charPositionInLine + this.stop.text.length - 1)

    return Location(this.startPosition(), this.endPosition())
}

private fun TerminalNode.location(): Location =
        this.symbol.location()

private fun Token.location(): Location =
        Location(Position(this.line, this.charPositionInLine), Position(this.line, this.charPositionInLine + this.text.length - 1))
