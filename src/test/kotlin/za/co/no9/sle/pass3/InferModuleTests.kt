package za.co.no9.sle.pass3

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.*
import za.co.no9.sle.pass1.toModule
import za.co.no9.sle.pass2.map


class InferModuleTests : StringSpec({
    "\"let add a b = a + b\nlet inc = add 1\"" {
        val environment =
                Environment(mapOf(
                        Pair("(+)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))))

        val module =
                map(toModule(za.co.no9.sle.parser.parseModule("let add a b = a + b\n" +
                        "let inc = add 1").right()!!.node))

        val inferResult =
                infer(module, environment).right()!!

        inferResult.second.map { it.toString() }
                .shouldBe(listOf(
                        "Int -> Int -> Int : '0 -> '2",
                        "'2 : '1 -> '3",
                        "'4 : Int -> '5"))
    }
})


