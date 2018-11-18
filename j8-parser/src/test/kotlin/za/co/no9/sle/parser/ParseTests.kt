package za.co.no9.sle.parser

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.specs.FunSpec
import za.co.no9.sle.*
import za.co.no9.sle.ast.typeless.Module
import java.util.function.Consumer


class ParseTests : FunSpec({
    runner(this, "parser", RunnerConsumer())
})


private class RunnerConsumer : Consumer<ConsumerParam> {
    override fun accept(param: ConsumerParam) {
        val fileContent =
                param.second

        val result =
                parseModule(Lexer(fileContent["src"]?.joinToString("\n") ?: ""))

        val astTest =
                fileContent["ast"]

        if (astTest != null) {
            result.shouldBeTypeOf<Either.Value<Module>>()
            result.right()!!.shouldBeEqual(astTest)
        }
    }
}
