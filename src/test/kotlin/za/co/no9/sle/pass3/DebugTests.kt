package za.co.no9.sle.pass3

import io.kotlintest.specs.StringSpec
import org.antlr.v4.runtime.misc.Utils.spaces
import za.co.no9.sle.*
import za.co.no9.sle.parser.parseModule
import za.co.no9.sle.pass1.toExpression
import za.co.no9.sle.pass1.toModule
import za.co.no9.sle.pass2.map


class Pass3DebugTests : StringSpec({
    fun inferExpression(input: String, env: Environment = emptyEnvironment): Either<Errors, Pair<Expression, Constraints>> =
            za.co.no9.sle.parser.parseExpression(input)
                    .map { toExpression(it.node) }
                    .map { map(it) }
                    .andThen { infer(it, env) }


    fun inferModule(input: String, env: Environment = emptyEnvironment): Either<Errors, Pair<Module, Constraints>> =
            parseModule(input)
                    .map { toModule(it.node) }
                    .map { map(it) }
                    .andThen { infer(it, env) }


    "Dump AST" {
        val environment =
                Environment(mapOf(
                        Pair("(+)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))),
                        Pair("(*)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))))

        val inferExpression =
                inferExpression("\\a b -> if False then a + b * 100 else a * b", environment)

        val inferExpressionAsString =
                inferExpression
                        .map { it.mapFirst { asString(it) } }
                        .map { it.mapSecond { it.toString() } }
                        .right() ?: Pair("", "")

        val substitution =
                inferExpression
                        .map { unifies(it.second) }
                        .right()!!

        val newAST =
                substitution
                        .andThen { ss ->
                            inferExpression.map { it.first.apply(ss) }
                        }
                        .map { asString(it) }
                        .right()

        println(inferExpressionAsString.first)
        println(inferExpressionAsString.second)
        println()
        println(newAST)
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
                inferModule
                        .right()!!
                        .mapFirst { asString(it) }
                        .mapSecond { it.toString() }

        val substitution =
                inferModule
                        .andThen { unifies(it.second) }

        val newAST =
                inferModule.right()!!.first.apply(substitution.right()!!)

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