package za.co.no9.sle

import io.kotlintest.specs.StringSpec
import org.antlr.v4.runtime.misc.Utils.spaces
import za.co.no9.sle.ast.pass3.*


class Pass3DebugTests : StringSpec({
    fun inferExpression(input: String, env: Environment = emptyEnvironment): Pair<String, List<Pair<String, String>>> {
        val expression =
                parseExpression(input)

        return Pair(asString(infer(expression, env).right()!!), za.co.no9.sle.ast.pass3.constraints(expression, env).map { Pair(it.first.toString(), it.second.toString()) })
    }


    "Dump AST" {
        val environment =
                mapOf(Pair("(+)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))))

        val inferExpression =
                inferExpression("\\a b -> a + b", environment)

        println(inferExpression.first)
        println(inferExpression.second.joinToString(",\n"))
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
