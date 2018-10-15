package za.co.no9.sle.transform.typedPatternToEnrichedCore

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.specs.FunSpec
import za.co.no9.sle.*
import za.co.no9.sle.typing.*
import java.util.function.Consumer


class TypedPatternToEnrichedCoreTests : FunSpec({
    runner(this, "typedPatternToEnrichedCore", RunnerConsumer())
})


private class RunnerConsumer : Consumer<Map<String, List<String>>> {
    private val environment =
            initialEnvironment
                    .newValue("(+)", Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))
                    .newValue("(-)", Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))
                    .newValue("(*)", Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))
                    .newValue("(/)", Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))
                    .newValue("(&&)", Scheme(listOf(), TArr(typeBool, TArr(typeBool, typeBool))))
                    .newValue("(==)", Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool))))
                    .newValue("aString", Scheme(listOf(), typeString))


    override fun accept(fileContent: Map<String, List<String>>) {
        val parseWithDetail =
                parseWithDetail(fileContent["src"]?.joinToString("\n") ?: "", environment)


        val constraints =
                fileContent["constraints"]

        if (constraints != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Detail>>()
            parseWithDetail.right()!!.constraints.state.map { it.toString() }.shouldBeEqual(constraints)
        }


        val expectedSubstitution =
                fileContent["substitution"]

        if (expectedSubstitution != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Detail>>()
            parseWithDetail.right()!!.substitution.state.map { it.toString() }.shouldBeEqual(expectedSubstitution)
        }


        val astTest =
                fileContent["ast"]

        if (astTest != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Detail>>()
            parseWithDetail.right()!!.unresolvedModule.shouldBeEqual(astTest)
        }


        val typeAST =
                fileContent["typeAST"]

        if (typeAST != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Detail>>()
            parseWithDetail.right()!!.resolvedModule.shouldBeEqual(typeAST)
        }


        val enrichedAST =
                fileContent["enrichedAST"]

        if (enrichedAST != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Detail>>()
            parseWithDetail.right()!!.enrichedModule.shouldBeEqual(enrichedAST)
        }


        val errors =
                fileContent["errors"]

        if (errors != null) {
            parseWithDetail.shouldBeTypeOf<Either.Error<Detail>>()
            parseWithDetail.left()!!.map { it.toString() }.shouldBeEqual(errors)
        }
    }
}


