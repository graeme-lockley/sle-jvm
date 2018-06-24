package za.co.no9.sle

import io.kotlintest.properties.Gen
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.ast.pass1.toExpression
import za.co.no9.sle.ast.pass2.Expression
import za.co.no9.sle.ast.pass2.map
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
})


fun inferExpression(input: String): Type =
        infer(parseExpression(input))


fun parseExpression(input: String): Expression =
        map(toExpression(parseTextAsExpression(input).right()!!.node))