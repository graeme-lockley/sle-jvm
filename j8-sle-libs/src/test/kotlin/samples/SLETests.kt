package samples

import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.specs.AbstractFunSpec
import io.kotlintest.specs.FunSpec
import za.co.no9.sle.actors.*
import za.co.no9.sle.runtime.ActorUtil
import za.co.no9.sle.tools.test.Repository
import za.co.no9.sle.tools.test.runTests
import java.io.File


class SLETests : FunSpec({
    runner(this)
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

    RunnerHelper.ADDUNIT = addUnit
    RunnerHelper.ADDSUITE = addSuite
    RunnerHelper.ROOT = test

    runTests("samples.RunnerHelper", repository, testRoot)

    (ActorUtil.synchronousWait as java.util.function.Function<Int, Any>).apply(10)
}


class Runner(val root: AbstractFunSpec) {
    val path =
            mutableListOf<String>()

    fun describe(d: Array<*>) {
        path += d[1] as String

        var current: Array<*> =
                d[2] as Array<*>

        while (current[0] as Int != 0) {
            runner(current[1] as Array<*>)
            current = current[2] as Array<*>
        }

        path.removeAt(path.size - 1)
    }

    fun test(t: Array<*>) {
        synchronized(root) {
            root.test(path.joinToString(":") + ":" + (t[1] as String)) {
                (t[2] as Boolean).shouldBeTrue()
            }
        }
    }

    fun runner(u: Array<*>) {
        if (u[0] as Int == 0) {
            describe(u)
        } else {
            test(u)
        }
    }
}


fun runnerOverUnit(root: AbstractFunSpec, unit: Array<*>) {
    Runner(root).runner(unit)
}


val addUnit = { root: AbstractFunSpec, x: Any ->
    runnerOverUnit(root, x as Array<*>)
}


val addSuite: (AbstractFunSpec, Any) -> Unit = { root: AbstractFunSpec, x: Any ->
    ActorUtil.controller.create(SuiteActor(root, x))
}


class SuiteActor(val root: AbstractFunSpec, val suite: Any) : ActorFunction<Boolean, Any> {
    override fun init(self: ActorRef<Boolean, Any>?): InitResult<Boolean> =
            InitResult(true, listOf(Cmd(suite as ActorRef<*, Array<*>>, arrayOf(1, self, 0) as Array<*>)))

    override fun update(state: Boolean?, message: Any?): Response<Boolean>? {
        runnerOverUnit(root, (message as Array<*>)[2] as Array<*>)

        return NoneResponse.INSTANCE as Response<Boolean>
    }

}
