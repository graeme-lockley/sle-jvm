package za.co.no9.sle

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.parser.Result
import za.co.no9.sle.parser.parseModule


class ParserTests : StringSpec({
    "the sle text \"identity x = x\" should parse" {
        val parseResult =
                parseModule("identity x = x")

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        parseResult.right()?.stringTree.shouldBe("(module (declaration identity x = (expression (term x))))")
    }


    "multiplicatives have higher precedence than additives" {
        val parseResult =
                parseModule("f a b c = a + b * c")

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        parseResult.right()?.stringTree.shouldBe("(module (declaration f a b c = (expression (expression (term a)) + (expression (expression (term b)) * (expression (term c))))))")
    }


    "a syntax error is reported" {
        val parseResult =
                parseModule("let identity 10 = x")

        parseResult.shouldBeTypeOf<Either.Error<String>>()
    }


    "a rudimentary type alias" {
        val parseResult =
                parseModule("typealias IntToInt = Int -> Int\nnegate a = 0 - a")

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
    }
})