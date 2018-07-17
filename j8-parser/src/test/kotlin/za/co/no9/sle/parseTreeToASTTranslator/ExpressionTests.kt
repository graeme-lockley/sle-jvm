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


    "\"\\x y -> x\" should produce AST LambdaExpression" {
        assertParseExpression(
                "\\x y -> x",
                "LambdaExpression(location=[(1, 0) (1, 8)], arguments=[ID(location=[(1, 1) (1, 1)], name=x), ID(location=[(1, 3) (1, 3)], name=y)], expression=IdReference(location=[(1, 8) (1, 8)], name=x))")
    }


    "\"x || y\" should produce AST LambdaExpression" {
        assertParseExpression(
                "x || y",
                "BinaryOpExpression(location=[(1, 0) (1, 5)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=ID(location=[(1, 2) (1, 3)], name=||), right=IdReference(location=[(1, 5) (1, 5)], name=y))")
    }


    "\"x && y\" should produce AST LambdaExpression" {
        assertParseExpression(
                "x && y",
                "BinaryOpExpression(location=[(1, 0) (1, 5)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=ID(location=[(1, 2) (1, 3)], name=&&), right=IdReference(location=[(1, 5) (1, 5)], name=y))")
    }


    "\"x == y\" should produce AST LambdaExpression" {
        assertParseExpression(
                "x == y",
                "BinaryOpExpression(location=[(1, 0) (1, 5)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=ID(location=[(1, 2) (1, 3)], name===), right=IdReference(location=[(1, 5) (1, 5)], name=y))")
    }


    "\"x != y\" should produce AST LambdaExpression" {
        assertParseExpression(
                "x != y",
                "BinaryOpExpression(location=[(1, 0) (1, 5)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=ID(location=[(1, 2) (1, 3)], name=!=), right=IdReference(location=[(1, 5) (1, 5)], name=y))")
    }


    "\"x <= y\" should produce AST LambdaExpression" {
        assertParseExpression(
                "x <= y",
                "BinaryOpExpression(location=[(1, 0) (1, 5)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=ID(location=[(1, 2) (1, 3)], name=<=), right=IdReference(location=[(1, 5) (1, 5)], name=y))")
    }


    "\"x < y\" should produce AST LambdaExpression" {
        assertParseExpression(
                "x < y",
                "BinaryOpExpression(location=[(1, 0) (1, 4)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=ID(location=[(1, 2) (1, 2)], name=<), right=IdReference(location=[(1, 4) (1, 4)], name=y))")
    }


    "\"x >= y\" should produce AST LambdaExpression" {
        assertParseExpression(
                "x >= y",
                "BinaryOpExpression(location=[(1, 0) (1, 5)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=ID(location=[(1, 2) (1, 3)], name=>=), right=IdReference(location=[(1, 5) (1, 5)], name=y))")
    }


    "\"x > y\" should produce AST LambdaExpression" {
        assertParseExpression(
                "x > y",
                "BinaryOpExpression(location=[(1, 0) (1, 4)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=ID(location=[(1, 2) (1, 2)], name=>), right=IdReference(location=[(1, 4) (1, 4)], name=y))")
    }


    "\"x + y\" should produce AST LambdaExpression" {
        assertParseExpression(
                "x + y",
                "BinaryOpExpression(location=[(1, 0) (1, 4)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=ID(location=[(1, 2) (1, 2)], name=+), right=IdReference(location=[(1, 4) (1, 4)], name=y))")
    }


    "\"x - y\" should produce AST LambdaExpression" {
        assertParseExpression(
                "x - y",
                "BinaryOpExpression(location=[(1, 0) (1, 4)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=ID(location=[(1, 2) (1, 2)], name=-), right=IdReference(location=[(1, 4) (1, 4)], name=y))")
    }


    "\"x * y\" should produce AST LambdaExpression" {
        assertParseExpression(
                "x * y",
                "BinaryOpExpression(location=[(1, 0) (1, 4)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=ID(location=[(1, 2) (1, 2)], name=*), right=IdReference(location=[(1, 4) (1, 4)], name=y))")
    }


    "\"x / y\" should produce AST LambdaExpression" {
        assertParseExpression(
                "x / y",
                "BinaryOpExpression(location=[(1, 0) (1, 4)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=ID(location=[(1, 2) (1, 2)], name=/), right=IdReference(location=[(1, 4) (1, 4)], name=y))")
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
