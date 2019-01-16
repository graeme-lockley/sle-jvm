package za.co.no9.sle.transform.typelessPatternToTypedPattern

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.specs.FunSpec
import za.co.no9.sle.*
import za.co.no9.sle.typing.*
import java.util.function.Consumer


class InferenceTests : FunSpec({
    runner(this, "typelessPatternToTypedPattern", RunnerConsumer())
})


private class RunnerConsumer : Consumer<ConsumerParam> {
    private val environment =
            initialEnvironment
                    .newValue("+",
                            OperatorBinding(
                                    Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))),
                                    6,
                                    Left
                            ))
                    .newValue("-",
                            OperatorBinding(
                                    Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))),
                                    6,
                                    Left
                            ))
                    .newValue("*",
                            OperatorBinding(
                                    Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))),
                                    7,
                                    Left
                            ))
                    .newValue("/",
                            OperatorBinding(
                                    Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))),
                                    7,
                                    Left
                            ))
                    .newValue("&&",
                            OperatorBinding(
                                    Scheme(listOf(), TArr(typeBool, TArr(typeBool, typeBool))),
                                    3,
                                    Right
                            ))
                    .newValue("==",
                            OperatorBinding(
                                    Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool))),
                                    4,
                                    None
                            ))
                    .newValue(">=",
                            OperatorBinding(
                                    Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool))),
                                    4,
                                    None
                            ))
                    .newValue("<=",
                            OperatorBinding(
                                    Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool))),
                                    4,
                                    None
                            ))
                    .newValue("aString", VariableBinding(Scheme(listOf(), typeString)))


    override fun accept(param: ConsumerParam) {
        val sourceFile =
                param.first

        val fileContent =
                param.second

        val parseCallbackContainer =
                ParseCallbackContainer()

        val parseWithDetail =
                parse(parseCallbackContainer, TestItem(sourceFile, fileContent["src"]?.joinToString("\n")
                        ?: ""), environment)


        val constraints =
                fileContent["constraints"]

        if (constraints != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Any>>()
            parseCallbackContainer.constraints!!.state.map { it.toString() }.shouldBeEqual(constraints)
        }


        val expectedSubstitution =
                fileContent["substitution"]

        if (expectedSubstitution != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Any>>()
            parseCallbackContainer.substitution!!.state.map { it.toString() }.shouldBeEqual(expectedSubstitution)
        }


        val astTest =
                fileContent["ast"]

        if (astTest != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Any>>()
            parseCallbackContainer.unresolvedModule!!.shouldBeEqual(astTest)
        }


        val typeAST =
                fileContent["typeAST"]

        if (typeAST != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Any>>()
            parseWithDetail.right()!!.module.shouldBeEqual(typeAST)
        }


        val errors =
                fileContent["errors"]

        if (errors != null) {
            parseWithDetail.shouldBeTypeOf<Either.Error<Any>>()
            parseWithDetail.left()!!.map { it.toString() }.shouldBeEqual(errors)
        }
    }
}


class ParseCallbackContainer: ParseCallback {
    var unresolvedModule: za.co.no9.sle.ast.typedPattern.Module? =
            null

    var constraints: Constraints? =
            null

    var substitution: Substitution? =
            null


    override fun unresolvedTypedPatternModule(unresolvedModule: za.co.no9.sle.ast.typedPattern.Module) {
        this.unresolvedModule = unresolvedModule
    }

    override fun constraints(constraints: Constraints) {
        this.constraints = constraints
    }

    override fun substitution(substitution: Substitution) {
        this.substitution = substitution
    }

}





