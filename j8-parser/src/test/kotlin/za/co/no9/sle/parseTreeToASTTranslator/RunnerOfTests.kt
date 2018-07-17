package za.co.no9.sle.parseTreeToASTTranslator

import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.FunSpec
import org.antlr.v4.runtime.misc.Utils.spaces
import za.co.no9.sle.Either
import za.co.no9.sle.parser.Result
import za.co.no9.sle.right
import java.io.File
import kotlin.reflect.full.memberProperties

class RunnerOfTests : FunSpec({
    val testRoot =
            File(rootDirectory, "parseTreeToASTTranslator")

    testRoot.walk().filter { it.absolutePath.endsWith(".scenario") && it.isFile }.forEach {
        val fileContent =
                readFile(it)

        val namePrefix =
                it.parent.drop(testRoot.absolutePath.length + 1).split("/")

        test((namePrefix + (fileContent["title"] ?: listOf())).joinToString(": ")) {
            val result =
                    parse(fileContent["src"]?.joinToString("\n") ?: "")

            val astTest =
                    fileContent["ast"]

            if (astTest != null) {
                result.shouldBeTypeOf<Either.Value<Result>>()

                val actual =
                        dumpString(result.right()!!)
                                .split("\n")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                                .joinToString("\n")

                val expected =
                        astTest
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                                .joinToString("\n")

                if (actual == expected) {
                    actual.shouldBe(expected)
                } else {
                    dumpString(result.right()!!)
                            .shouldBe(astTest.joinToString("\n"))
                }
            }
        }
    }
})


private val rootDirectory =
        File(File(System.getProperty("user.dir")), listOf("src", "test", "resources").joinToString(File.separator))


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


private fun dumpString(o: Any?, indent: Int = 0): String {
    fun dd(label: String, value: Any?, indent: Int): String =
            if (value == null) {
                "${spaces(indent)}$label: null\n"
            } else if (value is Int) {
                "${spaces(indent)}$label: $value\n"
            } else if (value is Boolean) {
                "${spaces(indent)}$label: $value\n"
            } else if (value is String) {
                "${spaces(indent)}$label: $value\n"
            } else if (value is List<*>) {
                if (value.isEmpty()) {
                    "${spaces(indent)}$label: []\n"
                } else {
                    "${spaces(indent)}$label: [\n" +
                            value.joinToString("") { dumpString(it, indent + 2) } +
                            "${spaces(indent)}]\n"
                }
            } else {
                "${spaces(indent)}$label:\n" +
                        dumpString(value, indent + 2)
            }

    if (o == null) {
        return ""
    } else {
        return "${spaces(indent)}${o.javaClass.kotlin.simpleName}:\n" +
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

