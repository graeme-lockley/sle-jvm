package za.co.no9.sle.mojo

import za.co.no9.sle.Either
import za.co.no9.sle.Errors
import za.co.no9.sle.WriteFileError
import za.co.no9.sle.repository.Export
import za.co.no9.sle.repository.Source
import za.co.no9.sle.repository.toJsonString
import java.io.File


class Repository(
        private val sourcePrefix: File,
        private val targetRoot: File) : za.co.no9.sle.repository.Repository {

    override fun import(name: String): Either<Errors, Export> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun export(source: Source, inputFile: File, value: Export): Errors =
            write(source, inputFile, "json", toJsonString(value))


    private fun write(source: Source, inputFile: File, extension: String, value: String): Errors {
        val targetFileName =
                targetFileName(source, inputFile, extension)

        return try {
            targetFileName.parentFile.mkdirs()
            targetFileName.writeText(value)

            emptySet()
        } catch (e: Exception) {
            setOf(WriteFileError(e))
        }
    }


    fun targetFileName(source: Source, inputFile: File, extension: String): File {
        val separator =
                File.separator

        val sourcePrefixName =
                sourcePrefix.absolutePath

        val inputFilePath =
                inputFile.parentFile.absolutePath

        return if (inputFilePath.startsWith(sourcePrefixName))
            File("$targetRoot${separator}file$separator${inputFilePath.drop(sourcePrefixName.length)}$separator${inputFile.nameWithoutExtension}.$extension")
        else
            File("$targetRoot${separator}file$separator$inputFilePath$separator${inputFile.nameWithoutExtension}.$extension")
    }
}
