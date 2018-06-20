package za.co.no9.sle

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.ast.*


class ParserASTTests : StringSpec({
    "\"True\" should produce corresponding AST" {
        val parseResult =
                parseTextAsExpression("True")

        val expression =
                parseResult.right()!!.parserToAST().popExpression()

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        expression.shouldBeTypeOf<True>()
        expression.toString().shouldBe("True(location=[(1, 0) (1, 3)])")
    }


    "\"False\" should produce corresponding AST" {
        val parseResult =
                parseTextAsExpression("False")

        val expression =
                parseResult.right()!!.parserToAST().popExpression()

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        expression.shouldBeTypeOf<False>()
        expression.toString().shouldBe("False(location=[(1, 0) (1, 4)])")
    }


    "\"234\" should produce corresponding AST" {
        val parseResult =
                parseTextAsExpression("234")

        val expression =
                parseResult.right()!!.parserToAST().popExpression()

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        expression.shouldBeTypeOf<ConstantInt>()
        expression.toString().shouldBe("ConstantInt(location=[(1, 0) (1, 2)], value=234)")
    }


    "\"Hello World\" should produce AST ConstantString with value \"Hello World\"" {
        val parseResult =
                parseTextAsExpression("\"Hello World\"")

        val expression =
                parseResult.right()!!.parserToAST().popExpression()

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        expression.shouldBeTypeOf<ConstantString>()
        expression.toString().shouldBe("ConstantString(location=[(1, 0) (1, 12)], value=Hello World)")
    }


    "\"Hello\\\\ \\\"World\" should produce AST ConstantString with value \"Hello\\ \"World\"" {
        val parseResult =
                parseTextAsExpression("\"Hello\\\\ \\\" World\"")

        val expression =
                parseResult.right()!!.parserToAST().popExpression()

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        expression.shouldBeTypeOf<ConstantString>()
        expression.toString().shouldBe("ConstantString(location=[(1, 0) (1, 17)], value=Hello\\ \" World)")
    }


    "\"!True\" should produce AST NotExpression" {
        val parseResult =
                parseTextAsExpression("!True")

        val expression =
                parseResult.right()!!.parserToAST().popExpression()

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        expression.shouldBeTypeOf<NotExpression>()
        expression.toString().shouldBe("NotExpression(location=[(1, 0) (1, 4)], expression=True(location=[(1, 1) (1, 4)]))")
    }


    "\"a\" should produce AST IdReference" {
        val parseResult =
                parseTextAsExpression("a")

        val expression =
                parseResult.right()!!.parserToAST().popExpression()

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        expression.shouldBeTypeOf<IdReference>()
        expression.toString().shouldBe("IdReference(location=[(1, 0) (1, 0)], id=a)")
    }


    "\"(a)\" should produce AST IdReference" {
        val parseResult =
                parseTextAsExpression("(a)")

        val expression =
                parseResult.right()!!.parserToAST().popExpression()

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        expression.shouldBeTypeOf<IdReference>()
        expression.toString().shouldBe("IdReference(location=[(1, 1) (1, 1)], id=a)")
    }


    "\"if True then 1 else 2\" should produce AST IfExpression" {
        val parseResult =
                parseTextAsExpression("if True then 1 else 2")


        val expression =
                parseResult.right()!!.parserToAST().popExpression()

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        expression.shouldBeTypeOf<IfExpression>()
        expression.toString().shouldBe("IfExpression(location=[(1, 0) (1, 20)], guardExpression=True(location=[(1, 3) (1, 6)]), thenExpression=ConstantInt(location=[(1, 13) (1, 13)], value=1), elseExpression=ConstantInt(location=[(1, 20) (1, 20)], value=2))")
    }
})