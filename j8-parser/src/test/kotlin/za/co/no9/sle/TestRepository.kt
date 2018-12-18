package za.co.no9.sle

import za.co.no9.sle.repository.Export
import za.co.no9.sle.repository.Item
import za.co.no9.sle.repository.fromJsonString
import za.co.no9.sle.typing.typeBool
import za.co.no9.sle.typing.typeInt
import za.co.no9.sle.typing.typeString
import za.co.no9.sle.typing.typeUnit
import java.io.File


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


    override fun sourceURN(): URN =
            URN(File, inputFile.canonicalPath)


    override fun itemRelativeTo(name: String): Either<Errors, Item> {
        val relativeFile =
                File(inputFile.parentFile, name)

        val jsonFile =
                File(relativeFile.absolutePath + ".json")

        return when {
            jsonFile.exists() && jsonFile.isFile && jsonFile.canRead() ->
                value(TestItem(relativeFile))

            else ->
                error(setOf(UnableToReadFile(relativeFile)))
        }
    }


    override fun exports(): Export =
            fromJsonString(File(inputFile.absolutePath + ".json").readText())
}