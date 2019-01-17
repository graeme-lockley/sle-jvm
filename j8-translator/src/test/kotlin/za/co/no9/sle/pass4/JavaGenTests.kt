package za.co.no9.sle.pass4

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.*
import za.co.no9.sle.repository.Export
import za.co.no9.sle.repository.Item
import za.co.no9.sle.repository.fromJsonString
import za.co.no9.sle.transform.enrichedCoreToCore.EmptyParseCallback
import za.co.no9.sle.transform.enrichedCoreToCore.parse
import za.co.no9.sle.typing.*
import java.io.File


class JavaGenTests : StringSpec({
    val environment =
            initialEnvironment
                    .newValue("+",
                            OperatorBinding(
                                    Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))),
                                    6,
                                    Left
                            ))
                    .newValue("-",
                            OperatorBinding(
                                    Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))),
                                    6,
                                    Left
                            ))
                    .newValue("*",
                            OperatorBinding(
                                    Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))),
                                    7,
                                    Left
                            ))
                    .newValue("==",
                            OperatorBinding(
                                    Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool))),
                                    4,
                                    None
                            ))
                    .newValue("aString", VariableBinding(Scheme(listOf(), typeString)))


    fun stuff(name: String, environment: Environment) {
        val inputFile =
                File(".", "./src/test/resources/test/$name.sle")

        val result =
                parse(EmptyParseCallback(), TestItem(inputFile), environment)
                        .map { translate(it, "test", name) }
                        .map { it.toString() }

        result.right().shouldBe(
                File(".", "./src/test/resources/test/$name.java").readText())
    }

    "Compile test/First.sle" {
        stuff("First",
                initialEnvironment
                        .newValue("-",
                                OperatorBinding(
                                        Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))),
                                        6,
                                        Left
                                ))
                        .newValue("*",
                                OperatorBinding(
                                        Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))),
                                        7,
                                        Left
                                ))
                        .newValue("==",
                                OperatorBinding(
                                        Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool))),
                                        4,
                                        None
                                ))
                        .newValue("aString", VariableBinding(Scheme(listOf(), typeString)))
        )
    }


    "Compile test/TypeReference.sle" {
        stuff("TypeReference",
                initialEnvironment
                        .newValue("==",
                                OperatorBinding(
                                        Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool))),
                                        4,
                                        None
                                ))
        )
    }


    "Compile test/UnitValue.sle" {
        stuff("UnitValue", initialEnvironment)
    }


    "Compile test/ListType.sle" {
        stuff("ListType", initialEnvironment)
    }


    "Compile test/BinaryOperator.sle" {
        stuff("BinaryOperator", environment)
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


    override fun itemRelativeTo(name: String): Either<Errors, Item> {
        val relativeFile =
                File(inputFile.parentFile, name)

        val sleFile =
                File(relativeFile.absolutePath + ".sle")

        val jsonFile =
                File(relativeFile.absolutePath + ".json")

        return when {
            jsonFile.exists() && jsonFile.isFile && jsonFile.canRead() || sleFile.exists() && sleFile.isFile && sleFile.canRead() ->
                value(TestItem(relativeFile))

            else ->
                error(setOf(UnableToReadFile(relativeFile)))
        }
    }


    override fun sourceURN(): URN =
            URN(File, inputFile.canonicalPath)


    override fun sourceCode(): Either<Errors, String> =
            try {
                value(inputFile.readText())
            } catch (e: java.io.IOException) {
                error(setOf(IOException(e)))
            }


    override fun exports(): Either<Errors, Export> =
            try {
                value(fromJsonString(File(inputFile.absolutePath + ".json").readText()))
            } catch (e: java.io.IOException) {
                error(setOf(IOException(e)))
            }
}