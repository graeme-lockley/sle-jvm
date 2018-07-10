package za.co.no9.sle.pass3

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.pass1.parseTreeToAST
import za.co.no9.sle.pass2.astToCoreAST
import za.co.no9.sle.right
import za.co.no9.sle.typing.*


class InferModuleTests : StringSpec({
    "\"let add a b = a + b\"" {
        val environment =
                Environment(mapOf(
                        Pair("(+)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))))

        val module =
                astToCoreAST(parseTreeToAST(za.co.no9.sle.parser.parseModule("let add a b = a + b").right()!!.node))

        val inferResult =
                infer(module, environment).right()!!

        inferResult.second.toString()
                .shouldBe(
                        "Int -> Int -> Int : '0 -> '2, " +
                                "'2 : '1 -> '3")
    }


    "!\"let add a b : Int -> Int -> Int = something a b\"" {
        val environment =
                Environment(mapOf(
                        Pair("something", Schema(listOf(1), TArr(TVar(1), TArr(TVar(1), TVar(1)))))))

        val module =
                astToCoreAST(parseTreeToAST(za.co.no9.sle.parser.parseModule("let add a b : Int -> Int -> Int = something a b").right()!!.node))

        val inferResult =
                infer(module, environment).right()!!

        inferResult.second.toString()
                .shouldBe(
                        "Int -> Int -> Int : '0 -> '2, " +
                                "'2 : '1 -> '3, " +
                                "'4 : Int -> '5")
    }
})
