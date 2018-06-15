package za.co.no9.sle

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec


class ParserTest : StringSpec({
    "the sle text \"identity x = x\" should parse" {
        val parseResult =
                parseText("identity x = x")

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        parseResult.right()?.stringTree.shouldBe("(module (declaration identity x = (expression (factor x))))")
    }


    "multiplicatives have higher precedence than additives" {
        val parseResult =
                parseText("f a b c = a + b * c")

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        parseResult.right()?.stringTree.shouldBe("(module (declaration f a b c = (expression (expression (factor a)) + (expression (expression (factor b)) * (expression (factor c))))))")
    }


    "a syntax error is reported" {
        val parseResult =
                parseText("identity 10 = x")

        parseResult.shouldBeTypeOf<Either.Error<String>>()
    }
})