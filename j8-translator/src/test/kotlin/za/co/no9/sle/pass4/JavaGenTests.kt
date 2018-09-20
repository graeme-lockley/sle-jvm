package za.co.no9.sle.pass4

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.transform.typelessCoreToTypedCore.parseWithDetail
import za.co.no9.sle.map
import za.co.no9.sle.right
import za.co.no9.sle.typing.*


class JavaGenTests : StringSpec({
    fun stuff(name: String, environment: Environment) {
        val input =
                this.javaClass.getResource("/test/$name.sle").readText()

        val result =
                parseWithDetail(input, environment)
                        .map { translateToJava(it.resolvedModule, "test", name) }
                        .map { it.toString() }

        this.javaClass.getResource("/test/$name.java").readText()
                .shouldBe(result.right())

    }


    "Compile test/First.sle" {
        stuff("First", Environment(mapOf(
                Pair("(==)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeBool)))),
                Pair("(-)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))),
                Pair("(*)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))))))
    }


    "Compile test/TypeReference.sle" {
        stuff("TypeReference", Environment(mapOf(
                Pair("(==)", Schema(listOf(Parameter(1)), TArr(TVar(1), TArr(TVar(1), typeBool))))
        )))
    }


    "Compile test/UnitValue.sle" {
        stuff("UnitValue", emptyEnvironment)
    }
})
