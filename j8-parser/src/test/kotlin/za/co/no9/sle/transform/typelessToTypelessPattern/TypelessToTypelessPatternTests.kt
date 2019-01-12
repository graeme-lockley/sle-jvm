package za.co.no9.sle.transform.typelessToTypelessPattern

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.specs.FunSpec
import za.co.no9.sle.*
import za.co.no9.sle.ast.typeless.Module
import java.util.function.Consumer


class TypelessToTypelessPatternTests : FunSpec({
    runner(this, "typelessToTypelessPattern", RunnerConsumer())
})


private class RunnerConsumer : Consumer<ConsumerParam> {
    override fun accept(param: ConsumerParam) {
        val fileContent =
                param.second

        val result =
                parse(fileContent["src"]?.joinToString("\n") ?: "")

        val astTest =
                fileContent["ast"]

        if (astTest != null) {
            result.shouldBeTypeOf<Either.Value<Module>>()
            result.right()!!.shouldBeEqual(astTest)
        }

        val errors =
                fileContent["errors"]

        if (errors != null) {
            result.shouldBeTypeOf<Either.Error<Errors>>()
            result.left()!!.map { it.toString() }.shouldBeEqual(errors)
        }
    }
}


