package za.co.no9.sle.tools.build

import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.specs.StringSpec
import java.io.File

class BuildTests : StringSpec({
    val homeDir =
            File(System.getProperty("user.dir") ?: "")

    val testsResourcesDir =
            File(homeDir, "src/test/resources/tests")


    "An invalid source directory will report an error" {
        val log =
                TestLog()

        build(log, File(testsResourcesDir, "unknown"), File("target/output"))

        log.items.any { it.contains(Regex("^E: Source.*is not a valid directory$")) }.shouldBeTrue()
    }

    "A valid source let's walk it and compile" {
        val log =
                PrintLog()

        build(log, File(testsResourcesDir, "input"), File(homeDir, "target/output"))
    }
})


class PrintLog : Log {
    override fun error(message: String) {
        println("Error: $message")
    }

    override fun info(message: String) {
        println("info: $message")
    }
}


class TestLog : Log {
    val items =
            mutableListOf<String>()

    override fun info(message: String) {
        items.add("I: $message")
    }

    override fun error(message: String) {
        items.add("E: $message")
    }
}