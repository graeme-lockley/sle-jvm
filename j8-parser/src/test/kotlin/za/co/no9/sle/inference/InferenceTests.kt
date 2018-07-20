package za.co.no9.sle.inference

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.specs.FunSpec
import za.co.no9.sle.Either
import za.co.no9.sle.astToCoreAST.astToCoreAST
import za.co.no9.sle.parseTreeToASTTranslator.parse
import za.co.no9.sle.parser.Result
import za.co.no9.sle.right
import za.co.no9.sle.runner
import za.co.no9.sle.shouldBeEqual
import za.co.no9.sle.typing.*
import java.util.function.Consumer


class InferenceTests : FunSpec({
    runner(this, "inference", RunnerConsumer())
})


private class RunnerConsumer : Consumer<Map<String, List<String>>> {
    private val environment =
            Environment(mapOf(
                    Pair("(+)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))))


    private fun inferModuleFromText(input: String) =
            infer(VarPump(), astToCoreAST(parse(input).right()!!), environment)


    override fun accept(fileContent: Map<String, List<String>>) {
        val result =
                inferModuleFromText(fileContent["src"]?.joinToString("\n") ?: "")

        val constraints =
                fileContent["constraints"]

        if (constraints != null) {
            result.shouldBeTypeOf<Either.Value<Result>>()
            val thingy = result.right()!!.second.state.map { it.toString() }

            thingy.shouldBeEqual(constraints)
        }
    }
}


