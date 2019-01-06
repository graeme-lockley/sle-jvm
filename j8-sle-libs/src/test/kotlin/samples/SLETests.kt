package samples

import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.specs.AbstractFunSpec
import io.kotlintest.specs.FunSpec
import za.co.no9.sle.tools.test.Repository
import za.co.no9.sle.tools.test.TestEvents
import za.co.no9.sle.tools.test.runTests
import java.io.File


class SLETests : FunSpec({
    runner(this)
})


private fun calculateRootDirectory(): File {
    val userDir =
            System.getProperty("user.dir")

    return File(if (userDir.endsWith("j8-sle-libs")) {
        File(userDir)
    } else {
        File(userDir, "j8-sle-libs")
    }, listOf("src", "main", "sle").joinToString(File.separator))
}


private val rootDirectory =
        calculateRootDirectory()


fun runner(test: AbstractFunSpec) {
    val testRoot =
            rootDirectory

    val repository =
            Repository(rootDirectory, File(rootDirectory, "../../../target/generated-sources/sle/java"))

    runTests(repository, testRoot, ReportTestResults(test))
}


class ReportTestResults(val root: AbstractFunSpec) : TestEvents {
    var names =
            listOf<String>()

    override fun openDescription(name: String) {
        names += name
    }

    override fun closeDescription() {
        names = names.dropLast(1)
    }

    override fun test(name: String, result: Boolean) {
        root.test(names.joinToString(":") + ":" + name) {
            result.shouldBeTrue()
        }
    }
}
