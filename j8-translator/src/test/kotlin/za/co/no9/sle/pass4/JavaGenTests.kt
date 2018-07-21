package za.co.no9.sle.pass4

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.inference.parseWithDetail
import za.co.no9.sle.map
import za.co.no9.sle.right
import za.co.no9.sle.typing.*


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
                parseWithDetail(input, environment)
                        .map { translateToJava(it.resolvedModule, "test", "First") }
                        .map { it.toString() }

//        println(result.mapError { it.toString() })

        this.javaClass.getResource("/test/First.java").readText()
                .shouldBe(result.right())
    }
})