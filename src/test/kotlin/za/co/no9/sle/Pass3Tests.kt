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
                .shouldBe(typeBool)
    }

    "\"False\" infers to TCon Boolean" {
        inferExpression("False")
                .shouldBe(typeBool)
    }

    "\"<int>\" infers to TCon Int" {
        assertAll(Gen.positiveIntegers()) { n: Int ->
            inferExpression(n.toString())
                    .shouldBe(typeInt)
        }
    }

    "\"<string>\" infers to TCon String" {
        assertAll { s: String ->
            inferExpression("\"${s.markup()}\"")
                    .shouldBe(typeString)
        }
    }

    "\"a\" infers to TCon String where a is bound to Schema [] String" {
        inferExpression("a", Environment(mapOf(Pair("a", Schema(listOf(), typeString)))))
                .shouldBe(typeString)
    }

    "\"a\" infers to an UnboundVariable error where a not within the environment" {
        inferExpressionError("a")
                .shouldBe(UnboundVariable(Location(Position(1, 0)), "a"))
    }

    "\"if a then b else c\"" {
        val environment =
                Environment(mapOf(Pair("a", Schema(listOf(), TVar(1))), Pair("b", Schema(listOf(), TVar(2))), Pair("c", Schema(listOf(), TVar(3)))))

        inferExpression("if a then b else c", environment)
                .shouldBe(TVar(2))

        constraints("if a then b else c", environment)
                .shouldBe(listOf(Pair(TVar(1), typeBool), Pair(TVar(2), TVar(3))))
    }
})


fun inferExpression(input: String, env: Environment = emptyEnvironment): Type =
        infer(parseExpression(input), env).right()!!


fun constraints(input: String, env: Environment = emptyEnvironment): Constraints =
        za.co.no9.sle.ast.pass3.constraints(parseExpression(input), env)


fun inferExpressionError(input: String, env: Environment = emptyEnvironment): Error =
        infer(parseExpression(input), env).left()!!


fun parseExpression(input: String): Expression =
        map(toExpression(parseTextAsExpression(input).right()!!.node))


fun String.markup(): String =
        this.replace("\\", "\\\\").replace("\"", "\\\"")