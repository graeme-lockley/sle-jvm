package za.co.no9.sle


data class URN(val source: Source, val name: String, val version: String? = null) {
    constructor(input: String) : this(extractSource(input), extractName(input), extractVersion(input))

    fun impliedName(): String =
            name.takeLastWhile { it.isLetterOrDigit() || it == '_' }
}


private fun extractSource(input: String): Source {
    val indexOfFirstColon =
            input.indexOf(':')

    return when (input.substring(0, indexOfFirstColon)) {
        "file" ->
            Source.File

        "github" ->
            Source.Github

        else ->
            Source.File
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


enum class Source {
    File, Github, Resource
}