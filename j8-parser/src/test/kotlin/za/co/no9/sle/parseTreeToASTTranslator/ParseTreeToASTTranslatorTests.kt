package za.co.no9.sle.parseTreeToASTTranslator

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import za.co.no9.sle.Either
import za.co.no9.sle.dumpString
import za.co.no9.sle.parser.Result
import za.co.no9.sle.right
import za.co.no9.sle.runner
import java.util.function.Consumer


class ParseTreeToASTTranslatorTests : FunSpec({
    runner(this, "parseTreeToASTTranslator", RunnerConsumer())
})


private class RunnerConsumer : Consumer<Map<String, List<String>>> {
    override fun accept(fileContent: Map<String, List<String>>) {
        val result =
                parse(fileContent["src"]?.joinToString("\n") ?: "")

        val astTest =
                fileContent["ast"]

        if (astTest != null) {
            result.shouldBeTypeOf<Either.Value<Result>>()

            val actual =
                    dumpString(result.right()!!)
                            .split("\n")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .joinToString("\n")

            val expected =
                    astTest
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .joinToString("\n")

            if (actual == expected) {
                actual.shouldBe(expected)
            } else {
                dumpString(result.right()!!)
                        .shouldBe(astTest.joinToString("\n"))
            }
        }
    }
}


