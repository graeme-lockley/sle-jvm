package za.co.no9.sle.pass4

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.*
import za.co.no9.sle.parseTreeToASTTranslator.parse
import za.co.no9.sle.typing.*
import za.co.no9.sle.astToCoreAST.astToCoreAST
import za.co.no9.sle.inference.assignTypesToCoreAST


class JavaGenTests : StringSpec({
    "Compile test/First.sle" {
        val input =
                this.javaClass.getResource("/test/First.sle").readText()

        val environment =
                Environment(mapOf(
                        Pair("(==)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeBool)))),
                        Pair("(-)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))),
                        Pair("(*)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))))

        val result =
                parse(input)
                        .map { astToCoreAST(it) }
                        .andThen { it.assignTypesToCoreAST(VarPump(), environment) }
                        .map { translateToJava(it, "test", "First") }
                        .map { it.toString() }

//        println(result.mapError { it.toString() })

        this.javaClass.getResource("/test/First.java").readText()
                .shouldBe(result.right())
    }
})