package za.co.no9.sle

import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import za.co.no9.sle.ast.pass1.toExpression
import za.co.no9.sle.ast.pass2.Expression
import za.co.no9.sle.ast.pass2.map
import za.co.no9.sle.ast.pass3.infer


class Pass3Tests : FreeSpec({
    "infer" - {
        "\"True\" infers to TCon Boolean" {
            infer(parseExpression("True"))
                    .shouldBe(typeBool)
        }

        "\"False\" infers to TCon Boolean" {
            infer(parseExpression("False"))
                    .shouldBe(typeBool)
        }
    }
})


fun parseExpression(input: String): Expression =
        map(toExpression(parseTextAsExpression(input).right()!!.node))