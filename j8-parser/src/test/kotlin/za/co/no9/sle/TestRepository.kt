package za.co.no9.sle

import za.co.no9.sle.repository.Export
import za.co.no9.sle.repository.Item
import za.co.no9.sle.repository.Repository
import za.co.no9.sle.repository.fromJsonString
import za.co.no9.sle.typing.Environment
import java.io.File


class TestRepository(private val builtins: Environment) : Repository<TestItem> {
    override fun import(name: String): Either<Errors, Export> {
        TODO()
    }

    override fun item(source: Source, inputFile: File, qualifier: String?): TestItem =
            TestItem(inputFile, builtins, qualifier)
}


class TestItem(private val inputFile: File, private val builtins: Environment, private val qualifier: String?) : Item {
    override fun exports(): Export =
            fromJsonString(File(inputFile.absolutePath + ".json").readText(), builtins, qualifier)
}