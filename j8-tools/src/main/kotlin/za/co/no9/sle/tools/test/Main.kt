package za.co.no9.sle.tools.test


import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import za.co.no9.sle.URN
import za.co.no9.sle.actors.ActorRef
import za.co.no9.sle.right
import za.co.no9.sle.runtime.ActorUtil
import za.co.no9.sle.tools.build.Item
import java.io.File
import java.util.function.Function


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

    val runner by parser
            .storing("-R", "--runner", help = "class name of test runner")
            .default("file.tools.test.Runner")

    val wait by parser
            .storing("-w", "--wait", help = "duration in ms to wait for all actor messaging activity to subside before terminating tests")  { toInt() }
            .default(100)

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
                    runTests(runner, repository, File("."))
                } else {
                    tests.forEach {
                        runTests(runner, repository, File(it))
                    }
                }

                (ActorUtil.synchronousWait as java.util.function.Function<Int, Any>).apply(wait)

                System.exit(0)
            }
        }


fun runTests(runnerClassName: String, repository: Repository, file: File) {
    if (file.isDirectory) {
        file.walk().filter { it.isFile }.filter { it.name.endsWith("Test.sle") }.map { URN(it) }.forEach { runTest(runnerClassName, repository, it) }
    } else {
        runTest(runnerClassName, repository, URN(file))
    }
}


fun runTest(runnerClassName: String, repository: Repository, urn: URN) {
    val item =
            repository.item(urn)

    val rightItem =
            item.right()

    if (rightItem != null) {
        val className =
                rightItem.javaPackageName + "." + rightItem.className

        val clazz =
                Class.forName(className)

        val field =
                clazz.getField("suite")

        val value =
                field.get(null)

        callTestRunner(runnerClassName, value)
    }
}


fun callTestRunner(runnerClassName: String, value: Any) {
    val clazz =
            Class.forName(runnerClassName)

    if (value is ActorRef<*, *>) {
        val field =
                clazz.getField("addSuite")

        (field.get(null) as Function<Any, Any>).apply(value)

    } else {
        val field =
                clazz.getField("addUnit")

        (field.get(null) as Function<Any, Any>).apply(value)
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