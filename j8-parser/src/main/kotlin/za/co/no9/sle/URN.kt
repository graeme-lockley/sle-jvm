package za.co.no9.sle


data class URN(val source: Source, val name: String, val version: String? = null) {
    constructor(input: String) : this(extractSource(input), extractName(input), extractVersion(input))

    fun impliedName(): String =
            name.takeLastWhile { it.isLetterOrDigit() || it == '_' }


    fun className(): String =
            when (source) {
                File -> {
                    val canonicalInputFile =
                            java.io.File(name).canonicalFile

                    canonicalInputFile.nameWithoutExtension
                }

                Github ->
                    TODO()

                Resource ->
                    TODO()
            }


    fun packageName(sourcePrefix: java.io.File): List<String> =
            when (source) {
                File -> {
                    val sourcePrefixName =
                            sourcePrefix.absolutePath

                    val canonicalInputFile =
                            java.io.File(name).canonicalFile

                    val inputFilePath =
                            canonicalInputFile.parent

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
}


private fun splitPath(path: String): List<String> {
    val input =
            path.trim(java.io.File.separatorChar)

    return if (input.isBlank())
        emptyList()
    else
        input.split(java.io.File.separatorChar)
}

private fun extractSource(input: String): Source {
    val indexOfFirstColon =
            input.indexOf(':')

    return when (input.substring(0, indexOfFirstColon)) {
        "file" ->
            File

        "github" ->
            Github

        "resource" ->
            Resource

        else ->
            File
    }
}


private fun extractName(input: String): String {
    val indexOfFirstColon =
            input.indexOf(':')

    val indexOfLastColon =
            input.lastIndexOf(':')

    return if (indexOfFirstColon == indexOfLastColon)
        input.drop(indexOfFirstColon + 1)
    else
        input.substring(indexOfFirstColon + 1, indexOfLastColon)
}


private fun extractVersion(input: String): String? {
    val indexOfFirstColon =
            input.indexOf(':')

    val indexOfLastColon =
            input.lastIndexOf(':')

    return if (indexOfFirstColon == indexOfLastColon)
        null
    else
        input.substring(indexOfLastColon + 1)
}


sealed class Source


object File : Source() {
    override fun toString(): String =
            "File"
}


object Github : Source() {
    override fun toString(): String =
            "Github"
}


object Resource : Source() {
    override fun toString(): String =
            "Resource"
}
