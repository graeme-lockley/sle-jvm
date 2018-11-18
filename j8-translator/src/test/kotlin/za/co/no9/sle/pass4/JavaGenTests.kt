package za.co.no9.sle.pass4

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.*
import za.co.no9.sle.repository.Export
import za.co.no9.sle.repository.Item
import za.co.no9.sle.repository.Repository
import za.co.no9.sle.transform.enrichedCoreToCore.parseWithDetail
import za.co.no9.sle.typing.*
import java.io.File


class JavaGenTests : StringSpec({
    fun stuff(name: String, environment: Environment) {
        val input =
                this.javaClass.getResource("/test/$name.sle").readText()

        val result =
                parseWithDetail(TestRepository(), input, environment)
                        .map { translateToJava(it.coreModule, "test", name) }
                        .map { it.toString() }

        result.right().shouldBe(
                this.javaClass.getResource("/test/$name.java").readText())
    }

    "Compile test/First.sle" {
        stuff("First",
                initialEnvironment
                        .newValue("(==)", Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeBool))))
                        .newValue("(-)", Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))
                        .newValue("(*)", Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))
        )
    }


    "Compile test/TypeReference.sle" {
        stuff("TypeReference",
                initialEnvironment
                        .newValue("(==)", Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool))))
        )
    }


    "Compile test/UnitValue.sle" {
        stuff("UnitValue", initialEnvironment)
    }


    "Compile test/ListType.sle" {
        stuff("ListType", initialEnvironment)
    }
})


class TestRepository : Repository<TestItem> {
    override fun import(name: String): Either<Errors, Export> {
        TODO()
    }

    override fun item(source: Source, inputFile: File): TestItem =
            TestItem()
}


class TestItem : Item