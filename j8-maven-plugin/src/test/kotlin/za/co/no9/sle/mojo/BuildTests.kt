package za.co.no9.sle.mojo

import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.specs.StringSpec
import org.apache.maven.plugin.logging.Log
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
                TestLog()

        build(log, File(testsResourcesDir, "input"), File(homeDir, "target/output"))
    }
})


class TestLog : Log {
    val items =
            mutableListOf<String>()

    override fun warn(item: CharSequence?) {
        items.add("W: " + item.toString())
    }

    override fun warn(item: CharSequence?, p1: Throwable?) {
        items.add("W: " + item.toString())
    }

    override fun warn(item: Throwable?) {
        items.add("W: " + item.toString())
    }

    override fun info(item: CharSequence?) {
        items.add("I: $item")
    }

    override fun info(item: CharSequence?, p1: Throwable?) {
        items.add("I: $item")
    }

    override fun info(item: Throwable?) {
        items.add("I: $item")
    }

    override fun isInfoEnabled(): Boolean =
            true

    override fun isErrorEnabled(): Boolean =
            true

    override fun isWarnEnabled(): Boolean =
            true

    override fun error(item: CharSequence?) {
        items.add("E: $item")
    }

    override fun error(item: CharSequence?, p1: Throwable?) {
        items.add("E: $item")
    }

    override fun error(item: Throwable?) {
        items.add("E: $item")
    }

    override fun isDebugEnabled(): Boolean =
            true

    override fun debug(item: CharSequence?) {
        items.add("D: $item")
    }

    override fun debug(item: CharSequence?, p1: Throwable?) {
        items.add("D: $item")
    }

    override fun debug(item: Throwable?) {
        items.add("D: $item")
    }
}