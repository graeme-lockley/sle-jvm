package za.co.no9.sle.mojo

import za.co.no9.sle.*
import za.co.no9.sle.repository.Export
import za.co.no9.sle.repository.fromJsonString
import java.io.File


abstract class Repository(
        open val sourcePrefix: File,
        open val targetRoot: File) : za.co.no9.sle.repository.Repository<Item> {
    override fun item(urn: URN): Either<Errors, Item> {
        val item =
                Item(this, urn)

        itemLoaded(item)

        return value(item)
    }


    abstract fun itemLoaded(item: Item)
}


class Item(
        private val repository: Repository,
        private val urn: URN) : za.co.no9.sle.repository.Item {

    val packageName =
            when (urn.source) {
                File -> {
                    val sourcePrefixName =
                            repository.sourcePrefix.absolutePath

                    val inputFilePath =
                            File(urn.name).parent

                    val innerPath =
                            if (inputFilePath.startsWith(sourcePrefixName))
                                splitPath(inputFilePath.drop(sourcePrefixName.length))
                            else
                                splitPath(inputFilePath)

                    listOf("file") + innerPath
                }

                Github ->
                    TODO()

                Resource ->
                    TODO()
            }


    val className =
            when (urn.source) {
                File ->
                    java.io.File(urn.name).nameWithoutExtension

                Github ->
                    TODO()

                Resource ->
                    TODO()
            }


    override fun sourceCode(): String =
            when (urn.source) {
                File ->
                    java.io.File(urn.name).readText()

                Github ->
                    TODO()

                Resource ->
                    TODO()
            }


    override fun sourceURN(): URN =
            urn


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


    override fun itemRelativeTo(name: String): Either<Errors, Item> =
            when (urn.source) {
                File -> {
                    val nameFile =
                            File(name)

                    if (nameFile.isAbsolute)
                        repository.item(URN(File, nameFile.canonicalPath))
                    else
                        repository.item(URN(File, File(File(urn.name).parentFile, "$name.sle").canonicalPath))
                }

                Github ->
                    TODO()

                Resource ->
                    TODO()
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


private fun splitPath(path: String): List<String> {
    val input =
            path.trim(java.io.File.separatorChar)

    return if (input.isBlank())
        emptyList()
    else
        input.split(java.io.File.separatorChar)
}
