package za.co.no9.sle

import za.co.no9.sle.repository.Export
import za.co.no9.sle.repository.Item
import za.co.no9.sle.repository.Repository
import za.co.no9.sle.repository.fromJsonString
import za.co.no9.sle.typing.typeBool
import za.co.no9.sle.typing.typeInt
import za.co.no9.sle.typing.typeString
import za.co.no9.sle.typing.typeUnit
import java.io.File


class TestRepository : Repository<TestItem> {
    override fun import(name: String): Either<Errors, Export> {
        TODO()
    }


    override fun item(source: Source, inputFile: File): TestItem =
            TestItem(inputFile)
}


class TestItem(private val inputFile: File, private val text: String?) : Item {
    constructor(inputFile: File) : this(inputFile, null)


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
            "file.package.name.File.$name"


    override fun sourceCode(): String =
            text ?: inputFile.readText()


    override fun sourceFile(): File =
            inputFile


    override fun itemRelativeTo(name: String): Item =
            TestItem(File(inputFile.parentFile, name))


    override fun exports(): Export =
            fromJsonString(File(inputFile.absolutePath + ".json").readText())
}