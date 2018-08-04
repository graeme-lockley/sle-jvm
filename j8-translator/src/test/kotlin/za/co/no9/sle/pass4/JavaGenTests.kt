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

        this.javaClass.getResource("/test/First.java").readText()
                .shouldBe(result.right())
    }

    "Compile test/TypeReference.sle" {
        val input =
                this.javaClass.getResource("/test/TypeReference.sle").readText()

        val environment =
                Environment(mapOf(
                        Pair("(==)", Schema(listOf(Parameter(1, null)), TArr(TVar(1), TArr(TVar(1), typeBool))))
                ))

        val result =
                parseWithDetail(input, environment)
                        .map { translateToJava(it.resolvedModule, "test", "TypeReference") }
                        .map { it.toString() }

        this.javaClass.getResource("/test/TypeReference.java").readText()
                .shouldBe(result.right())
    }

    "Compile test/UnitValue.sle" {
        val input =
                this.javaClass.getResource("/test/UnitValue.sle").readText()

        val environment =
                Environment(mapOf(
                        Pair("(==)", Schema(listOf(Parameter(1, null)), TArr(TVar(1), TArr(TVar(1), typeBool))))
                ))

        val result =
                parseWithDetail(input, environment)
                        .map { translateToJava(it.resolvedModule, "test", "UnitValue") }
                        .map { it.toString() }

        this.javaClass.getResource("/test/UnitValue.java").readText()
                .shouldBe(result.right())
    }
})