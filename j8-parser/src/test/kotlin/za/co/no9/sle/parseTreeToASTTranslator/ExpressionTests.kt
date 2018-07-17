package za.co.no9.sle.parseTreeToASTTranslator

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.Either
import za.co.no9.sle.parser.Result
import za.co.no9.sle.right


class ExpressionTests : StringSpec({
    fun assertParseExpression(input: String, output: String) {
        val parseResult =
                parseExpression(input)


        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        parseResult.right()!!.toString().shouldBe(output)
    }


    "\"identity 123\" should produce AST CallExpression" {
        assertParseExpression(
                "identity 123",
                "CallExpression(location=[(1, 0) (1, 11)], operator=IdReference(location=[(1, 0) (1, 7)], name=identity), operands=[ConstantInt(location=[(1, 9) (1, 11)], value=123)])")
    }


    "\"add 1 2\" should produce AST CallExpression" {
        assertParseExpression(
                "add 1 2",
                "CallExpression(location=[(1, 0) (1, 6)], operator=IdReference(location=[(1, 0) (1, 2)], name=add), operands=[CallExpression(location=[(1, 4) (1, 6)], operator=ConstantInt(location=[(1, 4) (1, 4)], value=1), operands=[ConstantInt(location=[(1, 6) (1, 6)], value=2)])])")
    }
})
