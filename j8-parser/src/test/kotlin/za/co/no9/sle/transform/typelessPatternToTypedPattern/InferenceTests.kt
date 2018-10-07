package za.co.no9.sle.transform.typelessPatternToTypedPattern

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.specs.FunSpec
import za.co.no9.sle.*
import za.co.no9.sle.typing.*
import java.util.function.Consumer


class InferenceTests : FunSpec({
    runner(this, "typelessPatternToTypedPattern", RunnerConsumer())
})


private class RunnerConsumer : Consumer<Map<String, List<String>>> {
    private val environment =
            initialEnvironment
                    .newValue("(+)", Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))
                    .newValue("(-)", Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))
                    .newValue("(*)", Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))
                    .newValue("(/)", Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))
                    .newValue("(&&)", Scheme(listOf(), TArr(typeBool, TArr(typeBool, typeBool))))
                    .newValue("(==)", Scheme(listOf(1), TArr(TVar(1), TArr(TVar(1), typeBool))))
                    .newValue("aString", Scheme(listOf(), typeString))


    override fun accept(fileContent: Map<String, List<String>>) {
        val parseWithDetail =
                parseWithDetail(fileContent["src"]?.joinToString("\n") ?: "", environment)


        val constraints =
                fileContent["constraints"]

        if (constraints != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<InferenceDetail>>()
            parseWithDetail.right()!!.constraints.state.map { it.toString() }.shouldBeEqual(constraints)
        }


        val expectedSubstitution =
                fileContent["substitution"]

        if (expectedSubstitution != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<InferenceDetail>>()
            parseWithDetail.right()!!.substitution.state.map { it.toString() }.shouldBeEqual(expectedSubstitution)
        }


        val astTest =
                fileContent["ast"]

        if (astTest != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<InferenceDetail>>()
            parseWithDetail.right()!!.unresolvedModule.shouldBeEqual(astTest)
        }


        val typeAST =
                fileContent["typeAST"]

        if (typeAST != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<InferenceDetail>>()
            parseWithDetail.right()!!.resolvedModule.shouldBeEqual(typeAST)
        }


        val errors =
                fileContent["errors"]

        if (errors != null) {
            parseWithDetail.shouldBeTypeOf<Either.Error<InferenceDetail>>()
            parseWithDetail.left()!!.map { it.toString() }.shouldBeEqual(errors)
        }
    }
}


