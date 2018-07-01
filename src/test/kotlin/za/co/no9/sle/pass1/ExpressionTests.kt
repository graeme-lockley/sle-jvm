package za.co.no9.sle.pass1

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.Either
import za.co.no9.sle.parser.Result
import za.co.no9.sle.parser.parseTextAsExpression
import za.co.no9.sle.right


class ExpressionTests : StringSpec({
    fun parseExpression(input: String, output: String) {
        val parseResult =
                parseTextAsExpression(input)

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        toExpression(parseResult.right()!!.node).toString().shouldBe(output)
    }


    "\"True\" should produce corresponding AST" {
        parseExpression(
                "True",
                "True(location=[(1, 0) (1, 3)])")
    }


    "\"False\" should produce corresponding AST" {
        parseExpression(
                "False",
                "False(location=[(1, 0) (1, 4)])")
    }


    "\"234\" should produce corresponding AST" {
        parseExpression(
                "234",
                "ConstantInt(location=[(1, 0) (1, 2)], value=234)")
    }


    "\"Hello World\" should produce AST ConstantString with value \"Hello World\"" {
        parseExpression(
                "\"Hello World\"",
                "ConstantString(location=[(1, 0) (1, 12)], value=Hello World)")
    }


    "\"Hello\\\\ \\\"World\" should produce AST ConstantString with value \"Hello\\ \"World\"" {
        parseExpression(
                "\"Hello\\\\ \\\" World\"",
                "ConstantString(location=[(1, 0) (1, 17)], value=Hello\\ \" World)")
    }


    "\"!True\" should produce AST NotExpression" {
        parseExpression(
                "!True",
                "NotExpression(location=[(1, 0) (1, 4)], expression=True(location=[(1, 1) (1, 4)]))")
    }


    "\"a\" should produce AST IdReference" {
        parseExpression(
                "a",
                "IdReference(location=[(1, 0) (1, 0)], name=a)")
    }


    "\"(a)\" should produce AST IdReference" {
        parseExpression(
                "(a)",
                "IdReference(location=[(1, 1) (1, 1)], name=a)")
    }


    "\"if True then 1 else 2\" should produce AST IfExpression" {
        parseExpression(
                "if True then 1 else 2",
                "IfExpression(location=[(1, 0) (1, 20)], guardExpression=True(location=[(1, 3) (1, 6)]), thenExpression=ConstantInt(location=[(1, 13) (1, 13)], value=1), elseExpression=ConstantInt(location=[(1, 20) (1, 20)], value=2))")
    }


    "\"\\x y -> x\" should produce AST LambdaExpression" {
        parseExpression(
                "\\x y -> x",
                "LambdaExpression(location=[(1, 0) (1, 8)], arguments=[ID(location=[(1, 1) (1, 1)], name=x), ID(location=[(1, 3) (1, 3)], name=y)], expression=IdReference(location=[(1, 8) (1, 8)], name=x))")
    }


    "\"x || y\" should produce AST LambdaExpression" {
        parseExpression(
                "x || y",
                "BinaryOpExpression(location=[(1, 0) (1, 5)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=ID(location=[(1, 2) (1, 3)], name=||), right=IdReference(location=[(1, 5) (1, 5)], name=y))")
    }


    "\"x && y\" should produce AST LambdaExpression" {
        parseExpression(
                "x && y",
                "BinaryOpExpression(location=[(1, 0) (1, 5)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=ID(location=[(1, 2) (1, 3)], name=&&), right=IdReference(location=[(1, 5) (1, 5)], name=y))")
    }


    "\"x == y\" should produce AST LambdaExpression" {
        parseExpression(
                "x == y",
                "BinaryOpExpression(location=[(1, 0) (1, 5)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=ID(location=[(1, 2) (1, 3)], name===), right=IdReference(location=[(1, 5) (1, 5)], name=y))")
    }


    "\"x != y\" should produce AST LambdaExpression" {
        parseExpression(
                "x != y",
                "BinaryOpExpression(location=[(1, 0) (1, 5)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=ID(location=[(1, 2) (1, 3)], name=!=), right=IdReference(location=[(1, 5) (1, 5)], name=y))")
    }


    "\"x <= y\" should produce AST LambdaExpression" {
        parseExpression(
                "x <= y",
                "BinaryOpExpression(location=[(1, 0) (1, 5)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=ID(location=[(1, 2) (1, 3)], name=<=), right=IdReference(location=[(1, 5) (1, 5)], name=y))")
    }


    "\"x < y\" should produce AST LambdaExpression" {
        parseExpression(
                "x < y",
                "BinaryOpExpression(location=[(1, 0) (1, 4)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=ID(location=[(1, 2) (1, 2)], name=<), right=IdReference(location=[(1, 4) (1, 4)], name=y))")
    }


    "\"x >= y\" should produce AST LambdaExpression" {
        parseExpression(
                "x >= y",
                "BinaryOpExpression(location=[(1, 0) (1, 5)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=ID(location=[(1, 2) (1, 3)], name=>=), right=IdReference(location=[(1, 5) (1, 5)], name=y))")
    }


    "\"x > y\" should produce AST LambdaExpression" {
        parseExpression(
                "x > y",
                "BinaryOpExpression(location=[(1, 0) (1, 4)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=ID(location=[(1, 2) (1, 2)], name=>), right=IdReference(location=[(1, 4) (1, 4)], name=y))")
    }


    "\"x + y\" should produce AST LambdaExpression" {
        parseExpression(
                "x + y",
                "BinaryOpExpression(location=[(1, 0) (1, 4)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=ID(location=[(1, 2) (1, 2)], name=+), right=IdReference(location=[(1, 4) (1, 4)], name=y))")
    }


    "\"x - y\" should produce AST LambdaExpression" {
        parseExpression(
                "x - y",
                "BinaryOpExpression(location=[(1, 0) (1, 4)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=ID(location=[(1, 2) (1, 2)], name=-), right=IdReference(location=[(1, 4) (1, 4)], name=y))")
    }


    "\"x * y\" should produce AST LambdaExpression" {
        parseExpression(
                "x * y",
                "BinaryOpExpression(location=[(1, 0) (1, 4)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=ID(location=[(1, 2) (1, 2)], name=*), right=IdReference(location=[(1, 4) (1, 4)], name=y))")
    }


    "\"x / y\" should produce AST LambdaExpression" {
        parseExpression(
                "x / y",
                "BinaryOpExpression(location=[(1, 0) (1, 4)], left=IdReference(location=[(1, 0) (1, 0)], name=x), operator=ID(location=[(1, 2) (1, 2)], name=/), right=IdReference(location=[(1, 4) (1, 4)], name=y))")
    }


    "\"identity 123\" should produce AST CallExpression" {
        parseExpression(
                "identity 123",
                "CallExpression(location=[(1, 0) (1, 11)], operator=IdReference(location=[(1, 0) (1, 7)], name=identity), operands=[ConstantInt(location=[(1, 9) (1, 11)], value=123)])")
    }


    "\"add 1 2\" should produce AST CallExpression" {
        parseExpression(
                "add 1 2",
                "CallExpression(location=[(1, 0) (1, 6)], operator=IdReference(location=[(1, 0) (1, 2)], name=add), operands=[CallExpression(location=[(1, 4) (1, 6)], operator=ConstantInt(location=[(1, 4) (1, 4)], value=1), operands=[ConstantInt(location=[(1, 6) (1, 6)], value=2)])])")
    }
})
