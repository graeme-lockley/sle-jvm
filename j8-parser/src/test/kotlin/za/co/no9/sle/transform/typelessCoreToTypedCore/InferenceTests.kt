package za.co.no9.sle.transform.typelessCoreToTypedCore

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.specs.FunSpec
import za.co.no9.sle.*
import za.co.no9.sle.parser.Result
import za.co.no9.sle.typing.*
import java.util.function.Consumer


class InferenceTests : FunSpec({
    runner(this, "typelessCoreToTypedCore", RunnerConsumer())
})


private class RunnerConsumer : Consumer<Map<String, List<String>>> {
    private val environment =
            Environment(mapOf(
                    Pair("(+)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))),
                    Pair("(-)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))),
                    Pair("(*)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))),
                    Pair("(/)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))),
                    Pair("(&&)", Schema(listOf(), TArr(typeBool, TArr(typeBool, typeBool)))),
                    Pair("(==)", Schema(listOf(1), TArr(TVar(1), TArr(TVar(1), typeBool)))),
                    Pair("aString", Schema(listOf(), typeString))
            ))


    override fun accept(fileContent: Map<String, List<String>>) {
        val parseWithDetail =
                parseWithDetail(fileContent["src"]?.joinToString("\n") ?: "", environment)


        val constraints =
                fileContent["constraints"]

        if (constraints != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Result>>()
            parseWithDetail.right()!!.constraints.state.map { it.toString() }.shouldBeEqual(constraints)
        }


        val expectedSubstitution =
                fileContent["substitution"]

        if (expectedSubstitution != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Result>>()
            parseWithDetail.right()!!.substitution.state.map { it.toString() }.shouldBeEqual(expectedSubstitution)
        }


        val astTest =
                fileContent["ast"]

        if (astTest != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Result>>()
            parseWithDetail.right()!!.unresolvedModule.shouldBeEqual(astTest)
        }


        val typeAST =
                fileContent["typeAST"]

        if (typeAST != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Result>>()
            parseWithDetail.right()!!.resolvedModule.shouldBeEqual(typeAST)
        }


        val errors =
                fileContent["errors"]

        if (errors != null) {
            parseWithDetail.shouldBeTypeOf<Either.Error<Result>>()
            parseWithDetail.left()!!.map { it.toString() }.shouldBeEqual(errors)
        }
    }
}


