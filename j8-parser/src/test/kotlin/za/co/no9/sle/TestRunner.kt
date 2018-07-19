package za.co.no9.sle

import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.AbstractFunSpec
import org.antlr.v4.runtime.misc.Utils.spaces
import java.io.File
import java.util.function.Consumer
import kotlin.reflect.full.memberProperties


fun runner(test: AbstractFunSpec, directory: String, process: Consumer<Map<String, List<String>>>) {
    val testRoot =
            File(za.co.no9.sle.rootDirectory, directory)

    testRoot.walk().filter { it.absolutePath.endsWith(".scenario") && it.isFile }.forEach {
        val fileContent =
                za.co.no9.sle.readFile(it)

        val namePrefix =
                it.parent.drop(testRoot.absolutePath.length + 1).split("/")

        test.test((namePrefix + (fileContent["title"] ?: listOf())).joinToString(": ")) {
            process.accept(fileContent)
        }
    }
}


private fun calculateRootDirectory(): File {
    val userDir =
            System.getProperty("user.dir")

    return File(if (userDir.endsWith("j8-parser")) {
        File(userDir)
    } else {
        File(userDir, "j8-parser")
    }, listOf("src", "test", "resources").joinToString(File.separator))
}


private val rootDirectory =
        calculateRootDirectory()


private fun readFile(file: File): Map<String, List<String>> {
    val fileContent =
            file.readLines()

    fileContent.size.shouldNotBe(0)
    fileContent[0].startsWith("--").shouldBeTrue()

    val result =
            mutableMapOf<String, List<String>>()

    var label =
            "src"

    val rows =
            mutableListOf<String>()


    result["title"] = listOf(
            file.name.dropLast(9),
            fileContent[0].drop(2).trim())

    fileContent.drop(1).forEach { row ->
        if (row.startsWith("--")) {
            result[label] = rows.toList()
            label = row.drop(2).trim()
            rows.clear()
        } else {
            rows.add(row)
        }
    }

    result[label] = rows.toList()

    return result.toMap()
}


fun dumpString(o: Any?, indent: Int = 0): String {
    fun dd(label: String, value: Any?, indent: Int): String =
            when (value) {
                null ->
                    "${spaces(indent)}$label: null\n"

                is Int ->
                    "${spaces(indent)}$label: $value\n"

                is Boolean ->
                    "${spaces(indent)}$label: $value\n"

                is String ->
                    "${spaces(indent)}$label: $value\n"

                is List<*> ->
                    when {
                        value.isEmpty() ->
                            "${spaces(indent)}$label: []\n"

                        else ->
                            "${spaces(indent)}$label: [\n" +
                                    value.joinToString("") { dumpString(it, indent + 2) } +
                                    "${spaces(indent)}]\n"
                    }

                else -> "${spaces(indent)}$label:\n" +
                        dumpString(value, indent + 2)
            }

    return when (o) {
        null ->
            ""

        else ->
            "${spaces(indent)}${o.javaClass.kotlin.simpleName}:\n" +
                    o.javaClass.kotlin.memberProperties.map {
                        try {
                            val value =
                                    it.call(o)

                            if (it.name == "location") {
                                ""
                            } else {
                                dd(it.name, value, indent + 2)
                            }
                        } catch (e: Exception) {
                            ""
                        }
                    }.filter { it.isNotEmpty() }.joinToString("")
    }
}

