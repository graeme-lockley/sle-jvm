package za.co.no9.sle.parser

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.specs.FunSpec
import za.co.no9.sle.Either
import za.co.no9.sle.ast.typeless.Module
import za.co.no9.sle.right
import za.co.no9.sle.runner
import za.co.no9.sle.shouldBeEqual
import java.util.function.Consumer


class ParseTests : FunSpec({
    runner(this, "parser", RunnerConsumer())
})


private class RunnerConsumer : Consumer<Map<String, List<String>>> {
    override fun accept(fileContent: Map<String, List<String>>) {
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