package za.co.no9.sle.tools.build

import za.co.no9.sle.*
import za.co.no9.sle.repository.Export
import za.co.no9.sle.repository.fromJsonString
import za.co.no9.sle.typing.*
import java.io.File


class Item(
        private val repository: Repository,
        private val urn: URN) : za.co.no9.sle.repository.Item {

    val packageName =
            derivePackage(repository, urn)

    val javaPackageName =
            packageName.joinToString(".")

    val className =
            File(urn.name).nameWithoutExtension


    override fun sourceCode(): Either<Errors, String> =
            try {
                value(when (urn.source) {
                    File ->
                        File(urn.name).readText()

                    Github ->
                        TODO()

                    Resource -> {
                        val resourceName =
                                if (urn.name.endsWith(".sle"))
                                    urn.name
                                else
                                    "${urn.name}.sle"

                        BuildRepository::class.java.getResource(resourceName).readText()
                    }
                })
            } catch (e: java.io.IOException) {
                error(setOf(IOException(e)))
            }


    override fun sourceURN(): URN =
            urn


    fun targetJavaFile(): File =
            File(File(repository.targetRoot, packageName.joinToString(File.separator)), "$className.java")


    fun targetJsonFile(): File =
            File(File(repository.targetRoot, packageName.joinToString(File.separator)), "$className.json")


    override fun exports(): Either<Errors, Export> =
            try {
                value(fromJsonString(targetJsonFile().readText()))
            } catch (e: java.io.IOException) {
                error(setOf(IOException(e)))
            }


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
            when (name) {
                "()" ->
                    typeUnit.name

                "Int" ->
                    typeInt.name

                "Bool" ->
                    typeBool.name

                "String" ->
                    typeString.name

                "Char" ->
                    typeChar.name

                else ->
                    if (packageName.isEmpty())
                        "$className.$name"
                    else
                        "$javaPackageName.$className.$name"
            }


    override fun resolveId(name: String): String =
            if (packageName.isEmpty())
                "$className.$name"
            else
                "$javaPackageName.$className.$name"
}


private fun derivePackage(repository: Repository, urn: URN): List<String> {
    val sourcePrefixName =
            repository.sourcePrefix.absolutePath

    val inputFilePath =
            File(urn.name).parent

    val innerPath =
            if (inputFilePath.startsWith(sourcePrefixName))
                splitPath(inputFilePath.drop(sourcePrefixName.length))
            else
                splitPath(inputFilePath)

    return listOf(when (urn.source) {
        File -> "file"
        Github -> "github"
        Resource -> "resource"
    }) + innerPath.map { it.filter { c -> c.isLetterOrDigit() } }.map { it.toLowerCase() }
}


private fun splitPath(path: String): List<String> {
    val input =
            path.trim(java.io.File.separatorChar)

    return if (input.isBlank())
        emptyList()
    else
        input.split(java.io.File.separatorChar)
}
