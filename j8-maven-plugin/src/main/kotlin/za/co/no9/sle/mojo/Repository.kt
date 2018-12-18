package za.co.no9.sle.mojo

import za.co.no9.sle.Either
import za.co.no9.sle.Errors
import za.co.no9.sle.Source
import za.co.no9.sle.repository.Export
import za.co.no9.sle.repository.fromJsonString
import za.co.no9.sle.value
import java.io.File


abstract class Repository(
        open val sourcePrefix: File,
        open val targetRoot: File) : za.co.no9.sle.repository.Repository<Item> {
    override fun item(source: Source, inputFile: File): Either<Errors, Item> {
        val sourcePrefixName =
                sourcePrefix.absolutePath

        val canonicalInputFile =
                inputFile.canonicalFile

        val inputFilePath =
                canonicalInputFile.parent

        val innerPath =
                if (inputFilePath.startsWith(sourcePrefixName))
                    splitPath(inputFilePath.drop(sourcePrefixName.length))
                else
                    splitPath(inputFilePath)

        val item =
                Item(this, canonicalInputFile, listOf("file") + innerPath, canonicalInputFile.nameWithoutExtension)

        itemLoaded(item)

        return value(item)
    }


    private fun splitPath(path: String): List<String> {
        val input =
                path.trim(File.separatorChar)

        return if (input.isBlank())
            emptyList()
        else
            input.split(File.separatorChar)
    }

    abstract fun itemLoaded(item: Item)
}


class Item(
        private val repository: Repository,
        private val inputFile: File,
        val packageName: List<String>,
        val className: String) : za.co.no9.sle.repository.Item {

    override fun sourceCode(): String =
            inputFile.readText()


    override fun sourceFile(): File =
            inputFile


    fun mustCompile(): Boolean {
        val targetJavaFile =
                targetJavaFile()

        val targetJsonFile =
                targetJsonFile()

        return !targetJavaFile.exists() || !targetJsonFile.exists() || sourceFile().lastModified() > targetJavaFile.lastModified()
    }


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