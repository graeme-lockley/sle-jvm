package za.co.no9.sle.pass3

import io.kotlintest.specs.StringSpec
import org.antlr.v4.runtime.misc.Utils.spaces
import za.co.no9.sle.*
import za.co.no9.sle.parser.parseModule
import za.co.no9.sle.pass1.toExpression
import za.co.no9.sle.pass1.toModule
import za.co.no9.sle.pass2.map


class Pass3DebugTests : StringSpec({
    fun parseExpression(input: String): Either<Error, za.co.no9.sle.pass2.Expression> =
            za.co.no9.sle.parser.parseExpression(input)
                    .map { toExpression(it.node) }
                    .map { map(it) }


    fun inferExpression(input: String, env: Environment = emptyEnvironment): Either<Errors, Pair<Expression, Constraints>> =
            parseExpression(input)
                    .mapError { listOf(it) }
                    .andThen { infer(it, env) }


    fun inferModule(input: String, env: Environment = emptyEnvironment): Pair<Module, Constraints> {
        val module =
                map(toModule(parseModule(input).right()!!.node))

        return infer(module, env).right()!!
    }


    fun Pair<Expression, Constraints>.asString(): Pair<String, String> =
            Pair(asString(this.first), this.second.toString())


    fun Pair<Module, Constraints>.asString(): Pair<String, String> =
            Pair(asString(this.first), this.second.toString())


    "Dump AST" {
        val environment =
                Environment(mapOf(
                        Pair("(+)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))),
                        Pair("(*)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))))

        val inferExpression =
                inferExpression("\\a b -> if False then a + b * 100 else a * b", environment)

        val inferExpressionAsString =
                inferExpression.right()!!.asString()

        val subst =
                unifies(inferExpression.right()!!.second)

        val newAST =
                apply(subst.right()!!, inferExpression.right()!!.first)

        println(inferExpressionAsString.first)
        println(inferExpressionAsString.second)
        println()
        println(asString(newAST))
    }


    "Dump Module AST" {
        val environment =
                Environment(mapOf(
                        Pair("(==)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeBool)))),
                        Pair("(-)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))),
                        Pair("(*)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))))

        val inferModule =
                inferModule("let factorial n =\n  if n == 0 then 1 else n * factorial (n - 1)", environment)

        val inferModuleAsString =
                inferModule.asString()

        val subst =
                unifies(inferModule.second)

        val newAST =
                apply(subst.right()!!, inferModule.first)

        println(inferModuleAsString.first)
        println(inferModuleAsString.second)
        println()
        println(asString(newAST))

        println()
        println(newAST.declarations.joinToString("\n") {
            when (it) {
                is LetDeclaration ->
                    "${it.name.name}: ${it.type}"
            }
        })
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

fun asString(module: Module, indent: Int = 0): String =
        module.declarations.joinToString("\n") {
            when (it) {
                is LetDeclaration ->
                    "${spaces(indent)}Let: ${it.type}\n" +
                            "${spaces(indent + 2)}${it.name.name}\n" +
                            asString(it.expression, indent + 2)
            }
        }
