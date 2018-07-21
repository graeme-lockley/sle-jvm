package za.co.no9.sle.inference

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.specs.FunSpec
import za.co.no9.sle.*
import za.co.no9.sle.astToCoreAST.astToCoreAST
import za.co.no9.sle.parseTreeToASTTranslator.parse
import za.co.no9.sle.parser.Result
import za.co.no9.sle.typing.*
import java.util.function.Consumer


class InferenceTests : FunSpec({
    runner(this, "inference", RunnerConsumer())
})


private class RunnerConsumer : Consumer<Map<String, List<String>>> {
    private val environment =
            Environment(mapOf(
                    Pair("(+)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))),
                    Pair("(&&)", Schema(listOf(), TArr(typeBool, TArr(typeBool, typeBool)))),
                    Pair("aString", Schema(listOf(), typeString))
            ))


    private fun inferModuleFromText(input: String) =
            infer(VarPump(), astToCoreAST(parse(input).right()!!), environment)


    override fun accept(fileContent: Map<String, List<String>>) {
        val result =
                inferModuleFromText(fileContent["src"]?.joinToString("\n") ?: "")

        val constraints =
                fileContent["constraints"]

        if (constraints != null) {
            result.shouldBeTypeOf<Either.Value<Result>>()
            result.right()!!.second.state.map { it.toString() }.shouldBeEqual(constraints)
        }

        val errors =
                fileContent["errors"]

        if (errors != null) {
            result.shouldBeTypeOf<Either.Error<Result>>()
            result.left()!!.map { it.toString() }.shouldBeEqual(errors)
        }
    }
}


