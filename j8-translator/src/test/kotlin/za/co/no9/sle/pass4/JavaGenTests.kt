package za.co.no9.sle.pass4

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.homeLocation
import za.co.no9.sle.map
import za.co.no9.sle.repository.Export
import za.co.no9.sle.repository.Item
import za.co.no9.sle.repository.fromJsonString
import za.co.no9.sle.right
import za.co.no9.sle.transform.enrichedCoreToCore.parseWithDetail
import za.co.no9.sle.typing.*
import java.io.File


class JavaGenTests : StringSpec({
    fun stuff(name: String, environment: Environment) {
        val inputFile =
                File(".", "./src/test/resources/test/$name.sle")

        val result =
                parseWithDetail(TestItem(inputFile), environment)
                        .map { translateToJava(it.coreModule, "test", name) }
                        .map { it.toString() }

        result.right().shouldBe(
                File(".", "./src/test/resources/test/$name.java").readText())
    }

    "Compile test/First.sle" {
        stuff("First",
                initialEnvironment
                        .newValue("(==)", VariableBinding(Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeBool)))))
                        .newValue("(-)", VariableBinding(Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))))
                        .newValue("(*)", VariableBinding(Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))))
        )
    }


    "Compile test/TypeReference.sle" {
        stuff("TypeReference",
                initialEnvironment
                        .newValue("(==)", VariableBinding(Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool)))))
        )
    }


    "Compile test/UnitValue.sle" {
        stuff("UnitValue", initialEnvironment)
    }


    "Compile test/ListType.sle" {
        stuff("ListType", initialEnvironment)
    }
})


class TestItem(private val inputFile: File) : Item {
    override fun resolveConstructor(name: String): String =
            when (name) {
                "()" ->
                    typeUnit.name

                "Int" ->
                    typeInt.name

                "Bool" ->
                    typeBool.name

                "String" ->
                    typeString.name

                else ->
                    "file.package.name.File.$name"
            }


    override fun resolveId(name: String): String =
            TODO("not implemented")


    override fun itemRelativeTo(name: String): Item =
            TestItem(File(inputFile.parentFile, name))


    override fun sourceFile(): File =
            inputFile


    override fun sourceCode(): String =
            inputFile.readText()


    override fun exports(): Export =
            fromJsonString(File(inputFile.absolutePath + ".json").readText())
}