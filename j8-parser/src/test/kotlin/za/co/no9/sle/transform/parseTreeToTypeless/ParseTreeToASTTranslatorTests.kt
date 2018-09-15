package za.co.no9.sle.transform.parseTreeToTypeless

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.specs.FunSpec
import za.co.no9.sle.Either
import za.co.no9.sle.parser.Result
import za.co.no9.sle.right
import za.co.no9.sle.runner
import za.co.no9.sle.shouldBeEqual
import java.util.function.Consumer


class ParseTreeToASTTranslatorTests : FunSpec({
    runner(this, "parseTreeToTypeless", RunnerConsumer())
})


private class RunnerConsumer : Consumer<Map<String, List<String>>> {
    override fun accept(fileContent: Map<String, List<String>>) {
        val result =
                parse(fileContent["src"]?.joinToString("\n") ?: "")

        val astTest =
                fileContent["ast"]

        if (astTest != null) {
            result.shouldBeTypeOf<Either.Value<Result>>()
            result.right()!!.shouldBeEqual(astTest)
        }
    }
}


