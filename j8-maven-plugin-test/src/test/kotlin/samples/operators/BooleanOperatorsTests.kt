package samples.operators

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class BooleanOperatorsTests : StringSpec({
    "and True True returns true" {
        BooleanOperators.and.apply(true).apply(true)
                .shouldBe(true)
    }

    "and False True returns false" {
        BooleanOperators.and.apply(false).apply(true)
                .shouldBe(false)
    }

    "and True False returns false" {
        BooleanOperators.and.apply(true).apply(false)
                .shouldBe(false)
    }

    "and False False returns false" {
        BooleanOperators.and.apply(false).apply(false)
                .shouldBe(false)
    }


    "or True True returns true" {
        BooleanOperators.or.apply(true).apply(true)
                .shouldBe(true)
    }

    "or False True returns true" {
        BooleanOperators.or.apply(false).apply(true)
                .shouldBe(true)
    }

    "or True False returns true" {
        BooleanOperators.or.apply(true).apply(false)
                .shouldBe(true)
    }

    "or False False returns false" {
        BooleanOperators.or.apply(false).apply(false)
                .shouldBe(false)
    }
})