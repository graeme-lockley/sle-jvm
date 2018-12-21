package za.co.no9.sle.tools.test


import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import za.co.no9.sle.URN
import za.co.no9.sle.right
import za.co.no9.sle.tools.build.Item
import java.io.File


class Arguments(parser: ArgParser) {
    val verbose by parser
            .flagging("enable verbose mode")
            .default(false)

    val target by parser
            .storing("-T", "--target", help = "target directory of compiled code")
            .default(System.getenv("HOME") + "/.sle")
            .addValidator {
                val directory: java.io.File =
                        java.io.File(value)

                if (directory.exists() && directory.isFile) {
                    throw InvalidArgumentException("Target directory is not writable: $value")
                }
            }

    val source by parser
            .storing("-S", "--source", help = "source directory of files to compile")
            .default(File(".").absolutePath)

    val tests by parser
            .positionalList("test files to run")
            .default(emptyList())
}


fun main(arguments: Array<String>) =
        mainBody {
            val parsedArgs = ArgParser(arguments).parseInto(::Arguments)

            parsedArgs.run {
                val log =
                        Log()

                if (verbose) {
                    log.info("source: $source")
                    log.info("target: $target")
                }

                val repository =
                        Repository(File(source), File(target))

                if (tests.isEmpty()) {
                    runTests(repository, File("."))
                } else {
                    tests.forEach {
                        runTests(repository, File(it))
                    }
                }
            }
        }


fun runTests(repository: Repository, file: File) {
    if (file.isDirectory) {
        file.walk().filter { it.isFile }.filter { it.name.endsWith("Test.sle") }.map { URN(it) }.forEach { runTest(repository, it) }
    } else {
        runTest(repository, URN(file))
    }
}


fun runTest(repository: Repository, urn: URN) {
    val item =
            repository.item(urn)

    val rightItem =
            item.right()

    if (rightItem != null) {
        val className =
                rightItem.packageName.joinToString(".") + "." + rightItem.className

        val clazz =
                Class.forName(className)

        val field =
                clazz.getField("suite")

        val value =
                field.get(null)

        printResults(value as Array<*>)
    }
}


const val ANSI_RESET =
        "\u001B[0m"

const val ANSI_RED =
        "\u001B[31m"

const val ANSI_GREEN =
        "\u001B[32m"


fun printResults(value: Array<*>, indent: Int = 0) {
    when {
        value[0] == 0 -> {
            println("  ".repeat(indent) + value[1] as String)

            var current =
                    value[2] as Array<*>

            while (current[0] == 1) {
                printResults(current[1] as Array<*>, indent + 1)
                current = current[2] as Array<*>
            }
        }

        value[2] as Boolean ->
            println("  ".repeat(indent) + ANSI_GREEN + value[1] + ANSI_RESET)

        else ->
            println("  ".repeat(indent) + ANSI_RED + value[1] + ANSI_RESET)
    }
}


class Log : za.co.no9.sle.tools.build.Log {
    override fun error(message: String) {
        println("Error: $message")
    }

    override fun info(message: String) {
        println(message)
    }
}


class Repository(override val sourcePrefix: File,
                 override val targetRoot: File) : za.co.no9.sle.tools.build.Repository(sourcePrefix, targetRoot) {
    override fun itemLoaded(item: Item) {
    }
}