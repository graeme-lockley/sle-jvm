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
        private val targetRoot: File) : za.co.no9.sle.repository.Repository<ExportDetail> {
    override fun import(name: String): Either<Errors, Export> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun export(source: Source, inputFile: File, value: Export): Errors =
            write(source, inputFile, "json", toJsonString(value))


    override fun exportDetail(source: Source, inputFile: File): ExportDetail {
        val sourcePrefixName =
                sourcePrefix.absolutePath

        val inputFilePath =
                inputFile.parentFile.absolutePath

        fun splitPath(path: String): List<String> {
            val input =
                    path.trim(File.separatorChar)

            return if (input.isBlank())
                emptyList()
            else
                input.split(File.separatorChar)
        }


        return if (inputFilePath.startsWith(sourcePrefixName))
            ExportDetail(listOf("file") + splitPath(inputFilePath.drop(sourcePrefixName.length)), inputFile.nameWithoutExtension)
        else
            ExportDetail(listOf("file") + splitPath(inputFilePath), inputFile.nameWithoutExtension)
    }


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
        val exportDetail =
                exportDetail(source, inputFile)

        return File(File(targetRoot, exportDetail.packageName.joinToString(File.separator)), exportDetail.className + "." + extension)
    }
}


data class ExportDetail(val packageName: List<String>, val className: String)