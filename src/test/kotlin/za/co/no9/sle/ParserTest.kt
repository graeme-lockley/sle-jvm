package za.co.no9.sle

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec


class ParserTest : StringSpec({
    "the sle text \"let identity x = x\" should parse" {
        val parseResult =
                parseText("let identity x = x")

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        parseResult.right()?.stringTree.shouldBe("(module (declaration let identity x = (toExpression (factor x))))")
    }


    "multiplicatives have higher precedence than additives" {
        val parseResult =
                parseText("let f a b c = a + b * c")

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        parseResult.right()?.stringTree.shouldBe("(module (declaration let f a b c = (toExpression (toExpression (factor a)) + (toExpression (toExpression (factor b)) * (toExpression (factor c))))))")
    }


    "a syntax error is reported" {
        val parseResult =
                parseText("let identity 10 = x")

        parseResult.shouldBeTypeOf<Either.Error<String>>()
    }
})