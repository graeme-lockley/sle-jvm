package za.co.no9.sle

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.ast.True

class ParserASTTests : StringSpec({
    "\"True\" should produce corresponding AST" {
        val parseResult =
                parseTextAsFactor("True")

        val expression =
                parseResult.right()!!.parserToAST().popExpression()

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        expression.shouldBeTypeOf<True>()
        expression.toString().shouldBe("True(position=(1, 0))")
    }
})