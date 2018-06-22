package za.co.no9.sle

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec


class ParserASTTests : StringSpec({
    "\"True\" should produce corresponding AST" {
        parseExpressionSuccess(
                "True",
                "True(location=[(1, 0) (1, 3)])")
    }


    "\"False\" should produce corresponding AST" {
        parseExpressionSuccess(
                "False",
                "False(location=[(1, 0) (1, 4)])")
    }


    "\"234\" should produce corresponding AST" {
        parseExpressionSuccess(
                "234",
                "ConstantInt(location=[(1, 0) (1, 2)], value=234)")
    }


    "\"Hello World\" should produce AST ConstantString with value \"Hello World\"" {
        parseExpressionSuccess(
                "\"Hello World\"",
                "ConstantString(location=[(1, 0) (1, 12)], value=Hello World)")
    }


    "\"Hello\\\\ \\\"World\" should produce AST ConstantString with value \"Hello\\ \"World\"" {
        parseExpressionSuccess(
                "\"Hello\\\\ \\\" World\"",
                "ConstantString(location=[(1, 0) (1, 17)], value=Hello\\ \" World)")
    }


    "\"!True\" should produce AST NotExpression" {
        parseExpressionSuccess(
                "!True",
                "NotExpression(location=[(1, 0) (1, 4)], expression=True(location=[(1, 1) (1, 4)]))")
    }


    "\"a\" should produce AST IdReference" {
        parseExpressionSuccess(
                "a",
                "IdReference(location=[(1, 0) (1, 0)], name=a)")
    }


    "\"(a)\" should produce AST IdReference" {
        parseExpressionSuccess(
                "(a)",
                "IdReference(location=[(1, 1) (1, 1)], name=a)")
    }


    "\"if True then 1 else 2\" should produce AST IfExpression" {
        parseExpressionSuccess(
                "if True then 1 else 2",
                "IfExpression(location=[(1, 0) (1, 20)], guardExpression=True(location=[(1, 3) (1, 6)]), thenExpression=ConstantInt(location=[(1, 13) (1, 13)], value=1), elseExpression=ConstantInt(location=[(1, 20) (1, 20)], value=2))")
    }


    "\"\\x y -> x\" should produce AST LambdaExpression" {
        parseExpressionSuccess(
                "\\x y -> x",
                "LambdaExpression(location=[(1, 0) (1, 8)], arguments=[IdReference(location=[(1, 1) (1, 1)], name=x), IdReference(location=[(1, 3) (1, 3)], name=y)], expression=IdReference(location=[(1, 8) (1, 8)], name=x))")
    }


    "\"x || y\" should produce AST LambdaExpression" {
        parseExpressionSuccess(
                "x || y",
                "BinaryOpExpression(location=[(1, 0) (1, 5)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=IdReference(location=[(1, 2) (1, 3)], name=||), right=IdReference(location=[(1, 5) (1, 5)], name=y))")
    }


    "\"x && y\" should produce AST LambdaExpression" {
        parseExpressionSuccess(
                "x && y",
                "BinaryOpExpression(location=[(1, 0) (1, 5)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=IdReference(location=[(1, 2) (1, 3)], name=&&), right=IdReference(location=[(1, 5) (1, 5)], name=y))")
    }


    "\"x == y\" should produce AST LambdaExpression" {
        parseExpressionSuccess(
                "x == y",
                "BinaryOpExpression(location=[(1, 0) (1, 5)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=IdReference(location=[(1, 2) (1, 3)], name===), right=IdReference(location=[(1, 5) (1, 5)], name=y))")
    }


    "\"x != y\" should produce AST LambdaExpression" {
        parseExpressionSuccess(
                "x != y",
                "BinaryOpExpression(location=[(1, 0) (1, 5)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=IdReference(location=[(1, 2) (1, 3)], name=!=), right=IdReference(location=[(1, 5) (1, 5)], name=y))")
    }


    "\"x <= y\" should produce AST LambdaExpression" {
        parseExpressionSuccess(
                "x <= y",
                "BinaryOpExpression(location=[(1, 0) (1, 5)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=IdReference(location=[(1, 2) (1, 3)], name=<=), right=IdReference(location=[(1, 5) (1, 5)], name=y))")
    }


    "\"x < y\" should produce AST LambdaExpression" {
        parseExpressionSuccess(
                "x < y",
                "BinaryOpExpression(location=[(1, 0) (1, 4)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=IdReference(location=[(1, 2) (1, 2)], name=<), right=IdReference(location=[(1, 4) (1, 4)], name=y))")
    }


    "\"x >= y\" should produce AST LambdaExpression" {
        parseExpressionSuccess(
                "x >= y",
                "BinaryOpExpression(location=[(1, 0) (1, 5)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=IdReference(location=[(1, 2) (1, 3)], name=>=), right=IdReference(location=[(1, 5) (1, 5)], name=y))")
    }


    "\"x > y\" should produce AST LambdaExpression" {
        parseExpressionSuccess(
                "x > y",
                "BinaryOpExpression(location=[(1, 0) (1, 4)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=IdReference(location=[(1, 2) (1, 2)], name=>), right=IdReference(location=[(1, 4) (1, 4)], name=y))")
    }


    "\"x + y\" should produce AST LambdaExpression" {
        parseExpressionSuccess(
                "x + y",
                "BinaryOpExpression(location=[(1, 0) (1, 4)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=IdReference(location=[(1, 2) (1, 2)], name=+), right=IdReference(location=[(1, 4) (1, 4)], name=y))")
    }


    "\"x - y\" should produce AST LambdaExpression" {
        parseExpressionSuccess(
                "x - y",
                "BinaryOpExpression(location=[(1, 0) (1, 4)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=IdReference(location=[(1, 2) (1, 2)], name=-), right=IdReference(location=[(1, 4) (1, 4)], name=y))")
    }


    "\"x * y\" should produce AST LambdaExpression" {
        parseExpressionSuccess(
                "x * y",
                "BinaryOpExpression(location=[(1, 0) (1, 4)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=IdReference(location=[(1, 2) (1, 2)], name=*), right=IdReference(location=[(1, 4) (1, 4)], name=y))")
    }


    "\"x / y\" should produce AST LambdaExpression" {
        parseExpressionSuccess(
                "x / y",
                "BinaryOpExpression(location=[(1, 0) (1, 4)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=IdReference(location=[(1, 2) (1, 2)], name=/), right=IdReference(location=[(1, 4) (1, 4)], name=y))")
    }
})


private fun parseExpressionSuccess(input: String, output: String) {
    val parseResult =
            parseTextAsExpression(input)

    val expression =
            parseResult.right()!!.parserToAST().popExpression()

    parseResult.shouldBeTypeOf<Either.Value<Result>>()
    expression.toString().shouldBe(output)
}