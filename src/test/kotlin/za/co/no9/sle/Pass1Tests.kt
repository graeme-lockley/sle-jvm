package za.co.no9.sle

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.ast.pass1.toExpression
import za.co.no9.sle.ast.pass1.toModule


class Pass1Tests : StringSpec({
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
                "LambdaExpression(location=[(1, 0) (1, 8)], arguments=[ID(location=[(1, 1) (1, 1)], name=x), ID(location=[(1, 3) (1, 3)], name=y)], expression=IdReference(location=[(1, 8) (1, 8)], name=x))")
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


    "\"identity 123\" should produce AST CallExpression" {
        parseExpressionSuccess(
                "identity 123",
                "CallExpression(location=[(1, 0) (1, 11)], operator=IdReference(location=[(1, 0) (1, 7)], name=identity), operands=[ConstantInt(location=[(1, 9) (1, 11)], value=123)])")
    }


    "\"add 1 2\" should produce AST CallExpression" {
        parseExpressionSuccess(
                "add 1 2",
                "CallExpression(location=[(1, 0) (1, 6)], operator=IdReference(location=[(1, 0) (1, 2)], name=add), operands=[CallExpression(location=[(1, 4) (1, 6)], operator=ConstantInt(location=[(1, 4) (1, 4)], value=1), operands=[ConstantInt(location=[(1, 6) (1, 6)], value=2)])])")
    }


    "\"let add x y = x + y\" should product AST LetDeclaration" {
        parseSuccess(
                "let add x y = x + y",
                "Module(location=[(1, 0) (1, 18)], declarations=[LetDeclaration(location=[(1, 0) (1, 18)], name=ID(location=[(1, 4) (1, 6)], name=add), arguments=[ID(location=[(1, 8) (1, 8)], name=x), ID(location=[(1, 10) (1, 10)], name=y)], expression=BinaryOpExpression(location=[(1, 14) (1, 18)], left=IdReference(location=[(1, 14) (1, 14)], name=x), operator=IdReference(location=[(1, 16) (1, 16)], name=+), right=IdReference(location=[(1, 18) (1, 18)], name=y)))])"
        )
    }


    "\"let add x y = x + y\nlet sub a b = a - b\" should product AST Module" {
        parseSuccess(
                "let add x y = x + y\n" +
                        "let sub a b = a - b",
                "Module(location=[(1, 0) (2, 18)], declarations=[LetDeclaration(location=[(1, 0) (1, 18)], name=ID(location=[(1, 4) (1, 6)], name=add), arguments=[ID(location=[(1, 8) (1, 8)], name=x), ID(location=[(1, 10) (1, 10)], name=y)], expression=BinaryOpExpression(location=[(1, 14) (1, 18)], left=IdReference(location=[(1, 14) (1, 14)], name=x), operator=IdReference(location=[(1, 16) (1, 16)], name=+), right=IdReference(location=[(1, 18) (1, 18)], name=y))), LetDeclaration(location=[(2, 0) (2, 18)], name=ID(location=[(2, 4) (2, 6)], name=sub), arguments=[ID(location=[(2, 8) (2, 8)], name=a), ID(location=[(2, 10) (2, 10)], name=b)], expression=BinaryOpExpression(location=[(2, 14) (2, 18)], left=IdReference(location=[(2, 14) (2, 14)], name=a), operator=IdReference(location=[(2, 16) (2, 16)], name=-), right=IdReference(location=[(2, 18) (2, 18)], name=b)))])"
        )
    }
})


private fun parseExpressionSuccess(input: String, output: String) {
    val parseResult =
            parseTextAsExpression(input)

    parseResult.shouldBeTypeOf<Either.Value<Result>>()
    toExpression(parseResult.right()!!.node).toString().shouldBe(output)
}


private fun parseSuccess(input: String, output: String) {
    val parseResult =
            parseText(input)

    parseResult.shouldBeTypeOf<Either.Value<Result>>()
    toModule(parseResult.right()!!.node).toString().shouldBe(output)
}