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
                    Pair("(-)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))),
                    Pair("(*)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))),
                    Pair("(&&)", Schema(listOf(), TArr(typeBool, TArr(typeBool, typeBool)))),
                    Pair("(==)", Schema(listOf(1), TArr(TVar(1), TArr(TVar(1), typeBool)))),
                    Pair("aString", Schema(listOf(), typeString))
            ))


    private fun inferModuleFromText(varPump: VarPump, input: String) =
            infer(varPump, astToCoreAST(parse(input).right()!!), environment)

    override fun accept(fileContent: Map<String, List<String>>) {
        val varPump =
                VarPump()

        val result =
                inferModuleFromText(varPump, fileContent["src"]?.joinToString("\n") ?: "")

        val constraints =
                fileContent["constraints"]

        if (constraints != null) {
            result.shouldBeTypeOf<Either.Value<Result>>()
            result.right()!!.second.state.map { it.toString() }.shouldBeEqual(constraints)
        }

        val expectedSubstitution =
                fileContent["substitution"]

        val typeAST =
                fileContent["typeAST"]
        if (expectedSubstitution != null || typeAST != null) {
            result.shouldBeTypeOf<Either.Value<Result>>()
            val substitution =
                    result.andThen { unifies(varPump, emptyMap(), it.second) }

            if (expectedSubstitution != null) {
                substitution.right()!!.state.shouldBeEqual(expectedSubstitution)
            }
            if (typeAST != null) {
                result.right()!!.first.apply(substitution.right()!!)
                        .shouldBeEqual(typeAST)
            }
        }

        val errors =
                fileContent["errors"]

        if (errors != null) {
            result.shouldBeTypeOf<Either.Error<Result>>()
            result.left()!!.map { it.toString() }.shouldBeEqual(errors)
        }
    }
}


