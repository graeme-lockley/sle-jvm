package za.co.no9.sle

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.ast.*


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


    "\"False\" should produce corresponding AST" {
        val parseResult =
                parseTextAsFactor("False")

        val expression =
                parseResult.right()!!.parserToAST().popExpression()

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        expression.shouldBeTypeOf<False>()
        expression.toString().shouldBe("False(position=(1, 0))")
    }


    "\"234\" should produce corresponding AST" {
        val parseResult =
                parseTextAsFactor("234")

        val expression =
                parseResult.right()!!.parserToAST().popExpression()

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        expression.shouldBeTypeOf<ConstantInt>()
        expression.toString().shouldBe("ConstantInt(position=(1, 0), value=234)")
    }


    "\"Hello World\" should produce AST ConstantString with value \"Hello World\"" {
        val parseResult =
                parseTextAsFactor("\"Hello World\"")

        val expression =
                parseResult.right()!!.parserToAST().popExpression()

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        expression.shouldBeTypeOf<ConstantString>()
        expression.toString().shouldBe("ConstantString(position=(1, 0), value=Hello World)")
    }


    "\"Hello\\\\ \\\"World\" should produce AST ConstantString with value \"Hello\\ \"World\"" {
        val parseResult =
                parseTextAsFactor("\"Hello\\\\ \\\" World\"")

        val expression =
                parseResult.right()!!.parserToAST().popExpression()

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        expression.shouldBeTypeOf<ConstantString>()
        expression.toString().shouldBe("ConstantString(position=(1, 0), value=Hello\\ \" World)")
    }


    "\"!True\" should produce AST NotExpression" {
        val parseResult =
                parseTextAsFactor("!True")

        val expression =
                parseResult.right()!!.parserToAST().popExpression()

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        expression.shouldBeTypeOf<NotExpression>()
        expression.toString().shouldBe("NotExpression(position=(1, 0), expression=True(position=(1, 1)))")
    }


    "\"a\" should produce AST IdReference" {
        val parseResult =
                parseTextAsFactor("a")

        val expression =
                parseResult.right()!!.parserToAST().popExpression()

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        expression.shouldBeTypeOf<IdRefernce>()
        expression.toString().shouldBe("IdRefernce(position=(1, 0), id=a)")
    }
})