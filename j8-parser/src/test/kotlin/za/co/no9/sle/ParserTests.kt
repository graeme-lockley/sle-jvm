package za.co.no9.sle

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.parser.Result
import za.co.no9.sle.parser.parseModule


class ParserTests : StringSpec({
    "the sle text \"let identity x = x\" should parse" {
        val parseResult =
                parseModule("let identity x = x")

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        parseResult.right()?.stringTree.shouldBe("(module (declaration let identity x = (expression (term x))))")
    }


    "multiplicatives have higher precedence than additives" {
        val parseResult =
                parseModule("let f a b c = a + b * c")

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        parseResult.right()?.stringTree.shouldBe("(module (declaration let f a b c = (expression (expression (term a)) + (expression (expression (term b)) * (expression (term c))))))")
    }


    "a syntax error is reported" {
        val parseResult =
                parseModule("let identity 10 = x")

        parseResult.shouldBeTypeOf<Either.Error<String>>()
    }


    "a let declaration with a type schema" {
        val parseResult =
                parseModule("let f a b c : Int -> Int -> String -> Bool = a + b * c")

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
    }


    "a rudimentary type alias" {
        val parseResult =
                parseModule("typealias IntToInt = Int -> Int\nlet negate a : IntToInt = 0 - a")

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
    }
})