package za.co.no9.sle.mojo

import za.co.no9.sle.*
import za.co.no9.sle.repository.Export
import za.co.no9.sle.repository.fromJsonString
import java.io.File


abstract class Repository(
        open val sourcePrefix: File,
        open val targetRoot: File) : za.co.no9.sle.repository.Repository<Item> {
    override fun item(source: Source, inputFile: File): Either<Errors, Item> {
        val canonicalInputFile =
                inputFile.canonicalFile

        val item =
                Item(this, URN(source, inputFile.canonicalPath), canonicalInputFile)

        itemLoaded(item)

        return value(item)
    }


    abstract fun itemLoaded(item: Item)
}


class Item(
        private val repository: Repository,
        private val urn: URN,
        private val inputFile: File) : za.co.no9.sle.repository.Item {

    val packageName: List<String>
        get() = urn.packageName(repository.sourcePrefix)

    val className: String
        get() = urn.className()


    override fun sourceCode(): String =
            inputFile.readText()


    override fun sourceFile(): File =
            inputFile


    fun targetJavaFile(): File =
            File(File(repository.targetRoot, packageName.joinToString(File.separator)), "$className.java")


    fun targetJsonFile(): File =
            File(File(repository.targetRoot, packageName.joinToString(File.separator)), "$className.json")


    override fun exports(): Export =
            fromJsonString(targetJsonFile().readText())


    fun writeJava(output: String) {
        val targetJavaFile =
                targetJavaFile()

        targetJavaFile.parentFile.mkdirs()
        targetJavaFile.writeText(output)
    }


    fun writeJson(output: String) {
        val targetJsonFile =
                targetJsonFile()

        targetJsonFile.parentFile.mkdirs()
        targetJsonFile.writeText(output)
    }


    override fun itemRelativeTo(name: String): Either<Errors, Item> {
        val nameFile =
                File(name)

        return if (nameFile.isAbsolute)
            repository.item(za.co.no9.sle.File, nameFile)
        else
            repository.item(za.co.no9.sle.File, File(inputFile.parentFile, "$name.sle"))
    }


    override fun resolveConstructor(name: String): String =
            if (packageName.isEmpty())
                "$className.$name"
            else
                "${packageName.joinToString(".")}.$className.$name"


    override fun resolveId(name: String): String =
            if (packageName.isEmpty())
                "$className.$name"
            else
                "${packageName.joinToString(".")}.$className.$name"
}