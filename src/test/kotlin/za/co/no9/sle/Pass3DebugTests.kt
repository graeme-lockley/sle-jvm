package za.co.no9.sle

import io.kotlintest.specs.StringSpec
import org.antlr.v4.runtime.misc.Utils.spaces
import za.co.no9.sle.ast.pass3.*


class Pass3DebugTests : StringSpec({
    fun inferExpression(input: String, env: Environment = emptyEnvironment): Pair<Expression, Constraints> {
        val expression =
                parseExpression(input)

        return Pair(infer(expression, env).right()!!, za.co.no9.sle.ast.pass3.constraints(expression, env))
    }

    fun Pair<Expression, Constraints>.asString(): Pair<String, List<Pair<String, String>>> {
        return Pair(asString(this.first), this.second.map { Pair(it.first.toString(), it.second.toString()) })
    }


    "Dump AST" {
        val environment =
                mapOf(Pair("(+)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))))

        val inferExpression =
                inferExpression("\\a b -> a + b", environment)

        val inferExpressionAsString =
                inferExpression.asString()

        val subst =
                unifies(inferExpression.second)

        val newAST =
                apply(subst.right()!!, inferExpression.first)

        println(inferExpressionAsString.first)
        println(inferExpressionAsString.second.joinToString(",\n"))
        println()
        println(asString(newAST))
    }
})

fun asString(expression: Expression, indent: Int = 0): String =
        when (expression) {
            is ConstantBool ->
                "${spaces(indent)}ConstantBool: ${expression.type}\n" +
                        "${spaces(indent + 2)}${expression.value}\n"

            is ConstantInt ->
                "${spaces(indent)}ConstantInt: ${expression.type}\n" +
                        "${spaces(indent + 2)}${expression.value}\n"

            is ConstantString ->
                "${spaces(indent)}ConstantString: ${expression.type}\n" +
                        "${spaces(indent + 2)}${expression.value}\n"

            is IdReference ->
                "${spaces(indent)}IdReference: ${expression.type}\n" +
                        "${spaces(indent + 2)}${expression.name}\n"

            is IfExpression ->
                "${spaces(indent)}IfExpression: ${expression.type}\n" +
                        asString(expression.guardExpression, indent + 2) +
                        asString(expression.thenExpression, indent + 2) +
                        asString(expression.elseExpression, indent + 2)

            is LambdaExpression ->
                "${spaces(indent)}LambdaExpression: ${expression.type}\n" +
                        "${spaces(indent + 2)}${expression.argument.name}\n" +
                        asString(expression.expression, indent + 2)

            is CallExpression ->
                "${spaces(indent)}CallExpression: ${expression.type}\n" +
                        asString(expression.operator, indent + 2) +
                        asString(expression.operand, indent + 2)
        }
