package za.co.no9.sle

import za.co.no9.sle.repository.Export
import za.co.no9.sle.repository.Item
import za.co.no9.sle.repository.Repository
import za.co.no9.sle.repository.fromJsonString
import za.co.no9.sle.typing.*
import java.io.File


class TestRepository(private val builtins: Environment) : Repository<TestItem> {
    override fun import(name: String): Either<Errors, Export> {
        TODO()
    }


    override fun item(source: Source, inputFile: File, qualifier: String?): TestItem =
            TestItem(inputFile, builtins, qualifier)
}


class TestItem(private val inputFile: File, private val text: String?, private val builtins: Environment, private val qualifier: String? = null) : Item {

    constructor(inputFile: File, builtins: Environment, qualifier: String?) : this(inputFile, null, builtins, qualifier)


    override fun resolveConstructor(name: QString): String =
            if (name.qualifier == null)
                when(name.string) {
                    "()" ->
                        typeUnit.name

                    "Int" ->
                        typeInt.name

                    "Bool" ->
                        typeBool.name

                    "String" ->
                        typeString.name

                    else ->
                        name.string
                }
            else
                "${name.qualifier}.${name.string}"


    override fun sourceCode(): String =
            text ?: inputFile.readText()


    override fun sourceFile(): File =
            inputFile


    override fun exports(): Export =
            fromJsonString(File(inputFile.absolutePath + ".json").readText())
}