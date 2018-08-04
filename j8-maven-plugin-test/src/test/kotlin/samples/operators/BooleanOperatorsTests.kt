package samples.operators

import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.specs.StringSpec

class BooleanOperatorsTests : StringSpec({
    "and True True returns true" {
        BooleanOperators.and.apply(true).apply(true)
                .shouldBeTrue()
    }

    "and False True returns false" {
        BooleanOperators.and.apply(false).apply(true)
                .shouldBeFalse()
    }

    "and True False returns false" {
        BooleanOperators.and.apply(true).apply(false)
                .shouldBeFalse()
    }

    "and False False returns false" {
        BooleanOperators.and.apply(false).apply(false)
                .shouldBeFalse()
    }


    "or True True returns true" {
        BooleanOperators.or.apply(true).apply(true)
                .shouldBeTrue()
    }

    "or False True returns true" {
        BooleanOperators.or.apply(false).apply(true)
                .shouldBeTrue()
    }

    "or True False returns true" {
        BooleanOperators.or.apply(true).apply(false)
                .shouldBeTrue()
    }

    "or False False returns false" {
        BooleanOperators.or.apply(false).apply(false)
                .shouldBeFalse()
    }
})