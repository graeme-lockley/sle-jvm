package za.co.no9.sle.inference

import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.*
import za.co.no9.sle.astToCoreAST.astToCoreAST
import za.co.no9.sle.parseTreeToASTTranslator.parseExpression
import za.co.no9.sle.typing.*


class InferExpressionTests : StringSpec({
    fun infer(input: String, env: Environment): Either<Errors, Pair<Expression, Constraints>> =
            parseExpression(input)
                    .map { astToCoreAST(it) }
                    .andThen { za.co.no9.sle.inference.infer(VarPump(), it, env) }


    fun inferExpression(input: String, env: Environment = emptyEnvironment): Pair<String, String>? =
            infer(input, env)
                    .map { it.mapFirst { it.type.toString() } }
                    .map { it.mapSecond { it.toString() } }
                    .right()


    fun inferExpressionError(input: String, env: Environment = emptyEnvironment): Errors? =
            infer(input, env).left()


    fun String.markup(): String =
            this.replace("\\", "\\\\").replace("\"", "\\\"")


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
