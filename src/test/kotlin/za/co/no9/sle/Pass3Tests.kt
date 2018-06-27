package za.co.no9.sle

import io.kotlintest.properties.Gen
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.ast.pass1.toExpression
import za.co.no9.sle.ast.pass2.Expression
import za.co.no9.sle.ast.pass2.map
import za.co.no9.sle.ast.pass3.Constraints
import za.co.no9.sle.ast.pass3.infer


class Pass3Tests : StringSpec({
    "\"True\" infers to TCon Boolean" {
        inferExpression("True")
                .shouldBe(Pair(
                        typeBool,
                        emptyList<Constraints>()))
    }

    "\"False\" infers to TCon Boolean" {
        inferExpression("False")
                .shouldBe(Pair(
                        typeBool,
                        emptyList<Constraints>()))
    }

    "\"<int>\" infers to TCon Int" {
        assertAll(Gen.positiveIntegers()) { n: Int ->
            inferExpression(n.toString())
                    .shouldBe(Pair(
                            typeInt,
                            emptyList<Constraints>()))
        }
    }

    "\"<string>\" infers to TCon String" {
        assertAll { s: String ->
            inferExpression("\"${s.markup()}\"")
                    .shouldBe(Pair(
                            typeString,
                            emptyList<Constraints>()))
        }
    }

    "\"a\" infers to TCon String where a is bound to Schema [] String" {
        inferExpression("a", Environment(mapOf(Pair("a", Schema(listOf(), typeString)))))
                .shouldBe(Pair(
                        typeString,
                        emptyList<Constraints>()))
    }

    "\"if a then b else c\"" {
        val environment =
                Environment(mapOf(
                        Pair("a", Schema(listOf(), TVar(1))),
                        Pair("b", Schema(listOf(), TVar(2))),
                        Pair("c", Schema(listOf(), TVar(3)))))

        inferExpression("if a then b else c", environment)
                .shouldBe(Pair(
                        TVar(2),
                        listOf(
                                Pair(TVar(1), typeBool),
                                Pair(TVar(2), TVar(3)))))
    }

    "\"\\a -> a\"" {
        inferExpression("\\a -> a")
                .shouldBe(Pair(
                        TArr(TVar(0), TVar(0)),
                        listOf<Constraints>()))
    }

    "\"\\a -> a 10\"" {
        inferExpression("\\a -> a 10")
                .shouldBe(Pair(
                        TArr(TVar(0), TVar(1)),
                        listOf(
                                Pair(TVar(0), TArr(typeInt, TVar(1)))
                        )))
    }

    "\"a\" infers to an UnboundVariable error where a not within the environment" {
        inferExpressionError("a")
                .shouldBe(UnboundVariable(Location(Position(1, 0)), "a"))
    }
})


fun inferExpression(input: String, env: Environment = emptyEnvironment): Pair<Type, Constraints> {
    val expression =
            parseExpression(input)

    return Pair(infer(expression, env).right()!!, za.co.no9.sle.ast.pass3.constraints(expression, env))
}

fun inferExpressionError(input: String, env: Environment = emptyEnvironment): Error =
        infer(parseExpression(input), env).left()!!


fun parseExpression(input: String): Expression =
        map(toExpression(parseTextAsExpression(input).right()!!.node))


fun String.markup(): String =
        this.replace("\\", "\\\\").replace("\"", "\\\"")
