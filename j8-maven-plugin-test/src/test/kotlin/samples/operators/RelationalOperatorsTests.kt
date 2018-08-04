package samples.operators

import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.specs.StringSpec

class RelationalOperatorsTests : StringSpec({
    "equal 5 5 returns true" {
        RelationalOperators.equal.apply(5).apply(5)
                .shouldBeTrue()
    }

    "equal 5 6 returns false" {
        RelationalOperators.equal.apply(5).apply(6)
                .shouldBeFalse()
    }

    "notEqual 5 5 returns false" {
        RelationalOperators.notEqual.apply(5).apply(5)
                .shouldBeFalse()
    }

    "notEqual 5 6 returns true" {
        RelationalOperators.notEqual.apply(5).apply(6)
                .shouldBeTrue()
    }

    "equal \"Hello\" \"Hello\" returns true" {
        RelationalOperators.equal.apply("Hello").apply("Hello")
                .shouldBeTrue()
    }

    "equal \"Hello\" \"World\" returns false" {
        RelationalOperators.equal.apply("Hello").apply("World")
                .shouldBeFalse()
    }

    "notEqual \"Hello\" \"Hello\" returns false" {
        RelationalOperators.notEqual.apply("Hello").apply("Hello")
                .shouldBeFalse()
    }

    "notEqual \"Hello\" \"World\" returns true" {
        RelationalOperators.notEqual.apply("Hello").apply("World")
                .shouldBeTrue()
    }

    "equal True True returns true" {
        RelationalOperators.equal.apply(true).apply(true)
                .shouldBeTrue()
    }

    "equal True False returns false" {
        RelationalOperators.equal.apply(true).apply(false)
                .shouldBeFalse()
    }

    "notEqual True True returns false" {
        RelationalOperators.notEqual.apply(true).apply(true)
                .shouldBeFalse()
    }

    "notEqual True False returns true" {
        RelationalOperators.notEqual.apply(true).apply(false)
                .shouldBeTrue()
    }
})