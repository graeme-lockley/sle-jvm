package samples

import io.kotlintest.specs.AbstractFunSpec
import io.kotlintest.specs.FunSpec
import za.co.no9.sle.runtime.ActorUtil
import za.co.no9.sle.tools.test.Repository
import za.co.no9.sle.tools.test.runTests
import java.io.File


class SLETests : FunSpec({
    runner(this)
    (ActorUtil.synchronousWait as java.util.function.Function<Int, Any>).apply(10)
})


private fun calculateRootDirectory(): File {
    val userDir =
            System.getProperty("user.dir")

    val baseDirectory =
            if (userDir.endsWith("j8-sle-libs"))
                File(userDir)
            else
                File(userDir, "j8-sle-libs")

    return File(baseDirectory, listOf("src", "main", "sle").joinToString(File.separator))
}


private val rootDirectory =
        calculateRootDirectory()


fun runner(test: AbstractFunSpec) {
    val testRoot =
            rootDirectory

    val repository =
            Repository(rootDirectory, File(rootDirectory, "../../../target/generated-sources/sle/java"))

    runTests("file.tools.test.Runner", repository, testRoot)
}
