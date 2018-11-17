package samples.operators

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class BooleanOperatorsTests : StringSpec({
    "and True True returns true" {
        file.samples.operators.BooleanOperators.and.apply(true).apply(true)
                .shouldBe(true)
    }

    "and False True returns false" {
        file.samples.operators.BooleanOperators.and.apply(false).apply(true)
                .shouldBe(false)
    }

    "and True False returns false" {
        file.samples.operators.BooleanOperators.and.apply(true).apply(false)
                .shouldBe(false)
    }

    "and False False returns false" {
        file.samples.operators.BooleanOperators.and.apply(false).apply(false)
                .shouldBe(false)
    }


    "or True True returns true" {
        file.samples.operators.BooleanOperators.or.apply(true).apply(true)
                .shouldBe(true)
    }

    "or False True returns true" {
        file.samples.operators.BooleanOperators.or.apply(false).apply(true)
                .shouldBe(true)
    }

    "or True False returns true" {
        file.samples.operators.BooleanOperators.or.apply(true).apply(false)
                .shouldBe(true)
    }

    "or False False returns false" {
        file.samples.operators.BooleanOperators.or.apply(false).apply(false)
                .shouldBe(false)
    }
})