package za.co.no9.sle


data class URN(val source: Source, val name: String, val version: String? = null) {
    constructor(input: String) : this(extractSource(input), extractName(input), extractVersion(input))

    constructor(file: java.io.File) : this(File, file.canonicalPath)


    fun impliedName(): String =
            name.takeLastWhile { it.isLetterOrDigit() || it == '_' }
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
