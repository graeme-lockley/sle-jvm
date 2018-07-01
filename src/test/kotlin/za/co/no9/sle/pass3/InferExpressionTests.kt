package za.co.no9.sle.pass3

import io.kotlintest.properties.Gen
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.*
import za.co.no9.sle.pass1.toExpression
import za.co.no9.sle.pass2.Expression
import za.co.no9.sle.pass2.map


class InferExpressionTests : StringSpec({
    fun parseExpression(input: String): Expression =
            map(toExpression(za.co.no9.sle.parser.parseExpression(input).right()!!.node))


    fun inferExpression(input: String, env: Environment = emptyEnvironment): Pair<String, String> {
        val expression =
                parseExpression(input)

        return Pair(infer(expression, env).right()!!.type.toString(), constraints(expression, env).toString())
    }


    fun inferExpressionError(input: String, env: Environment = emptyEnvironment): Errors {
        val expression =
                parseExpression(input)

        val infer =
                infer(expression, env)

        return infer.left()!!
    }


    fun String.markup(): String =
            this.replace("\\", "\\\\").replace("\"", "\\\"")


    "\"True\" infers to TCon Boolean" {
        inferExpression("True")
                .shouldBe(Pair("Bool", ""))
    }


    "\"False\" infers to TCon Boolean" {
        inferExpression("False")
                .shouldBe(Pair("Bool", ""))
    }


    "\"<int>\" infers to TCon Int" {
        assertAll(Gen.positiveIntegers()) { n: Int ->
            inferExpression(n.toString())
                    .shouldBe(Pair("Int", ""))
        }
    }


    "\"<string>\" infers to TCon String" {
        assertAll { s: String ->
            inferExpression("\"${s.markup()}\"")
                    .shouldBe(Pair("String", ""))
        }
    }


    "\"a\" infers to TCon String where a is bound to Schema [] String" {
        inferExpression("a", Environment(mapOf(Pair("a", Schema(listOf(), typeString)))))
                .shouldBe(Pair("String", ""))
    }


    "\"if a then b else c\"" {
        val environment =
                Environment(mapOf(
                        Pair("a", Schema(listOf(), TVar(1))),
                        Pair("b", Schema(listOf(), TVar(2))),
                        Pair("c", Schema(listOf(), TVar(3)))))

        inferExpression("if a then b else c", environment)
                .shouldBe(Pair(
                        "'2",
                        "'1 : Bool, " +
                                "'2 : '3"))
    }


    "\"\\a -> a\"" {
        inferExpression("\\a -> a")
                .shouldBe(Pair(
                        "'0 -> '0",
                        ""))
    }


    "\"\\a -> a 10\"" {
        inferExpression("\\a -> a 10")
                .shouldBe(Pair(
                        "'0 -> '1",
                        "'0 : Int -> '1"))
    }


    "\"a + 1\" infers to an Int when (+) is added to the environment" {
        val environment =
                Environment(mapOf(
                        Pair("a", Schema(listOf(), typeInt)),
                        Pair("(+)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))))

        inferExpression("a + 1", environment)
                .shouldBe(Pair(
                        "'1",
                        "Int -> Int -> Int : Int -> '0, " +
                                "'0 : Int -> '1"))
    }


    "\"a\" infers to an UnboundVariable error where a not within the environment" {
        inferExpressionError("a")
                .shouldBe(listOf(
                        UnboundVariable(Location(Position(1, 0)), "a")))
    }


    "\"a + 1\" infers to an UnboundVariable error where a not within the environment" {
        inferExpressionError("a + 1")
                .shouldBe(listOf(
                        UnboundVariable(Location(Position(1, 2)), "(+)"),
                        UnboundVariable(Location(Position(1, 0)), "a")))
    }
})
