package za.co.no9.sle.mojo

import za.co.no9.sle.Either
import za.co.no9.sle.Errors
import za.co.no9.sle.Source
import za.co.no9.sle.repository.Export
import za.co.no9.sle.repository.fromJsonString
import java.io.File


class Repository(
        private val sourcePrefix: File,
        val targetRoot: File) : za.co.no9.sle.repository.Repository<Item> {
    override fun import(name: String): Either<Errors, Export> =
            TODO("not implemented")


    override fun item(source: Source, inputFile: File): Item {
        val sourcePrefixName =
                sourcePrefix.absolutePath

        val inputFilePath =
                inputFile.parentFile.absolutePath

        val innerPath =
                if (inputFilePath.startsWith(sourcePrefixName))
                    splitPath(inputFilePath.drop(sourcePrefixName.length))
                else
                    splitPath(inputFilePath)

        return Item(this, inputFile, listOf("file") + innerPath, inputFile.nameWithoutExtension)
    }


    private fun splitPath(path: String): List<String> {
        val input =
                path.trim(File.separatorChar)

        return if (input.isBlank())
            emptyList()
        else
            input.split(File.separatorChar)
    }
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


    override fun itemRelativeTo(name: String): Item =
            repository.item(Source.File, File(name).relativeTo(inputFile))


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