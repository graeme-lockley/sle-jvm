package za.co.no9.sle.transform.parseTreeToTypeless

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
    private val expressionStack =
            Stack<Expression>()

    private val patternStack =
            Stack<Pattern>()

    private val typeStack =
            Stack<TType>()

    private var typeConstructors =
            emptyList<TypeConstructor>()

    private val caseItems =
            Stack<CaseItem>()

    private val declarations =
            Stack<Declaration>()

    var module: Module? =
            null
        private set


    override fun exitTrueExpression(ctx: ParserParser.TrueExpressionContext?) =
            expressionStack.push(True(ctx!!.location()))

    override fun exitFalseExpression(ctx: ParserParser.FalseExpressionContext?) =
            expressionStack.push(False(ctx!!.location()))

    override fun exitConstantIntExpression(ctx: ParserParser.ConstantIntExpressionContext?) =
            expressionStack.push(ConstantInt(ctx!!.location(), ctx.text.toInt()))

    override fun exitConstantStringExpression(ctx: ParserParser.ConstantStringExpressionContext?) {
        val text =
                ctx!!.text
                        .drop(1)
                        .dropLast(1)
                        .replace("\\\\", "\\")
                        .replace("\\\"", "\"")

        expressionStack.push(ConstantString(ctx.location(), text))
    }

    override fun exitNotExpression(ctx: ParserParser.NotExpressionContext?) =
            expressionStack.push(NotExpression(ctx!!.location(), expressionStack.pop()))

    override fun exitLowerIDExpression(ctx: ParserParser.LowerIDExpressionContext?) =
            expressionStack.push(IdReference(ctx!!.location(), ctx.text))

    override fun exitUpperIDExpression(ctx: ParserParser.UpperIDExpressionContext?) =
            expressionStack.push(ConstructorReference(ctx!!.location(), ctx.text))


    override fun exitIfExpression(ctx: ParserParser.IfExpressionContext?) {
        val elseExpression =
                expressionStack.pop()

        val thenExpression =
                expressionStack.pop()

        val guardExpression =
                expressionStack.pop()

        expressionStack.push(IfExpression(ctx!!.location(), guardExpression, thenExpression, elseExpression))
    }

    override fun exitLambdaExpression(ctx: ParserParser.LambdaExpressionContext?) {
        val expression =
                expressionStack.pop()

        expressionStack.push(LambdaExpression(ctx!!.location(), ctx.LowerID().toList().map { ID(it.location(), it.text) }, expression))
    }

    override fun exitUnitValueExpression(ctx: ParserParser.UnitValueExpressionContext?) {
        expressionStack.push(Unit(ctx!!.location()))
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
                expressionStack.pop()

        val left =
                expressionStack.pop()

        expressionStack.push(BinaryOpExpression(ctx.location(), left, ID(op.location(), op.text), right))
    }

    override fun exitCallExpression(ctx: ParserParser.CallExpressionContext?) {
        val numberOfOperands =
                ctx!!.childCount - 1

        if (numberOfOperands > 0) {
            val operands =
                    expressionStack.popNReversed(numberOfOperands)

            val operator =
                    expressionStack.pop()

            expressionStack.push(CallExpression(ctx.location(), operator, operands))
        }
    }

    override fun exitCaseExpression(ctx: ParserParser.CaseExpressionContext?) {
        expressionStack.push(CaseExpression(ctx!!.location(), expressionStack.pop(), caseItems.popAllReversed()))
    }


    override fun exitCaseItem(ctx: ParserParser.CaseItemContext?) {
        caseItems.push(CaseItem(ctx!!.location(), patternStack.pop(), expressionStack.pop()))
    }


    private fun fv(type: TType): Set<String> =
            when (type) {
                is TUnit ->
                    emptySet()

                is TVarReference ->
                    setOf(type.name)

                is TConstReference ->
                    emptySet()

                is TArrow ->
                    fv(type.domain) + fv(type.range)
            }


    private fun generaliseType(type: TType?): TScheme? =
            if (type == null)
                null
            else
                TScheme(type.location, fv(type).toList(), type)


    override fun exitLetSignature(ctx: ParserParser.LetSignatureContext?) {
        val type =
                typeStack.pop()

        declarations.push(LetSignature(ctx!!.location(), ID(ctx.LowerID().location(), ctx.LowerID().text), generaliseType(type)!!))
    }


    override fun exitLetDeclaration(ctx: ParserParser.LetDeclarationContext?) {
        val names =
                ctx!!.LowerID().toList().map { ID(it.location(), it.text) }

        val expression =
                expressionStack.pop()

        declarations.push(LetDeclaration(ctx.location(), names[0], names.drop(1), expression))
    }


    override fun exitLetGuardDeclaration(ctx: ParserParser.LetGuardDeclarationContext?) {
        val names =
                ctx!!.LowerID().toList().map { ID(it.location(), it.text) }

        val numberOfGuards =
                ctx.expression().size / 2

        var guardedExpressions =
                emptyList<Pair<Expression, Expression>>()

        for (lp in 1..numberOfGuards) {
            val expression =
                    expressionStack.pop()

            val guard =
                    expressionStack.pop()

            guardedExpressions += Pair(guard, expression)
        }

        declarations.push(LetGuardDeclaration(ctx.location(), names[0], names.drop(1), guardedExpressions.asReversed()))
    }


    override fun exitTypeAliasDeclaration(ctx: ParserParser.TypeAliasDeclarationContext?) {
        val type =
                typeStack.pop()

        declarations.push(TypeAliasDeclaration(ctx!!.location(), ID(ctx.UpperID().location(), ctx.UpperID().text), generaliseType(type)!!))
    }


    override fun exitTypeDeclaration(ctx: ParserParser.TypeDeclarationContext?) {
        declarations.push(TypeDeclaration(ctx!!.location(), ID(ctx.UpperID().location(), ctx.UpperID().text), ctx.LowerID().map { ID(it.location(), it.text) }, typeConstructors))

        typeConstructors = emptyList()
    }


    override fun exitTypeConstructor(ctx: ParserParser.TypeConstructorContext?) {
        val types =
                typeStack.popNReversed(ctx!!.type().size)

        typeConstructors += TypeConstructor(ctx.location(), ID(ctx.UpperID().location(), ctx.UpperID().text), types)
    }


    override fun exitModule(ctx: ParserParser.ModuleContext?) {
        module = Module(ctx!!.location(), declarations.popAllReversed())
    }


    override fun exitConstantIntPattern(ctx: ParserParser.ConstantIntPatternContext?) {
        patternStack.push(ConstantIntPattern(ctx!!.location(), ctx.text.toInt()))
    }

    override fun exitTruePattern(ctx: ParserParser.TruePatternContext?) {
        patternStack.push(ConstantBoolPattern(ctx!!.location(), true))
    }

    override fun exitFalsePattern(ctx: ParserParser.FalsePatternContext?) {
        patternStack.push(ConstantBoolPattern(ctx!!.location(), false))
    }

    override fun exitConstantStringPattern(ctx: ParserParser.ConstantStringPatternContext?) {
        val text =
                ctx!!.text
                        .drop(1)
                        .dropLast(1)
                        .replace("\\\\", "\\")
                        .replace("\\\"", "\"")


        patternStack.push(ConstantStringPattern(ctx.location(), text))
    }

    override fun exitUnitPattern(ctx: ParserParser.UnitPatternContext?) {
        patternStack.push(ConstantUnitPattern(ctx!!.location()))
    }

    override fun exitLowerIDPattern(ctx: ParserParser.LowerIDPatternContext?) {
        patternStack.push(IdReferencePattern(ctx!!.location(), ctx.text))
    }

    override fun exitUpperIDPattern(ctx: ParserParser.UpperIDPatternContext?) {
        val patterns =
                patternStack.popNReversed(ctx!!.pattern().size)

        patternStack.push(ConstructorReferencePattern(ctx.location(), ctx.UpperID().text, patterns))
    }

    override fun exitUnitType(ctx: ParserParser.UnitTypeContext?) {
        typeStack.push(TUnit(ctx!!.location()))
    }


    override fun exitLowerIDType(ctx: ParserParser.LowerIDTypeContext?) {
        typeStack.push(TVarReference(ctx!!.location(), ctx.text))
    }


    override fun exitUpperIDType(ctx: ParserParser.UpperIDTypeContext?) {
        typeStack.push(TConstReference(ctx!!.location(), ID(ctx.UpperID().location(), ctx.UpperID().text), emptyList()))
    }


    override fun exitParameterTypeArgument(ctx: ParserParser.ParameterTypeArgumentContext?) {
        val types =
                typeStack.popNReversed(ctx!!.type().size)

        typeStack.push(TConstReference(ctx.location(), ID(ctx.UpperID().location(), ctx.UpperID().text), types))
    }


    override fun exitArrowType(ctx: ParserParser.ArrowTypeContext?) {
        val range =
                typeStack.pop()

        val domain =
                typeStack.pop()

        typeStack.push(TArrow(ctx!!.location(), domain, range))
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
