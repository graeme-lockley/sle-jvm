package za.co.no9.sle

import za.co.no9.sle.repository.Export
import za.co.no9.sle.repository.Item
import za.co.no9.sle.repository.Repository
import za.co.no9.sle.repository.fromJsonString
import java.io.File


class TestRepository : Repository<TestItem> {
    override fun import(name: String): Either<Errors, Export> {
        TODO()
    }

    override fun item(source: Source, inputFile: File): TestItem =
            TestItem(inputFile)
}


class TestItem(val inputFile: File) : Item {
    override fun exports(): Export =
            fromJsonString(File(inputFile.absolutePath + ".json").readText())
}