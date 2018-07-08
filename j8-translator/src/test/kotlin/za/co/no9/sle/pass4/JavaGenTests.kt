package za.co.no9.sle.pass4

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.*
import za.co.no9.sle.typing.*
import za.co.no9.sle.parser.parseModule
import za.co.no9.sle.pass1.parseTreeToAST
import za.co.no9.sle.pass2.astToCoreAST
import za.co.no9.sle.pass3.assignTypesToCoreAST


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
                parseModule(input)
                        .map { parseTreeToAST(it.node) }
                        .map { astToCoreAST(it) }
                        .andThen { it.assignTypesToCoreAST(environment) }
                        .map { translateToJava(it, "test", "First") }
                        .map { it.toString() }

//        println(result.mapError { it.toString() })

        this.javaClass.getResource("/test/First.java").readText()
                .shouldBe(result.right())
    }
})