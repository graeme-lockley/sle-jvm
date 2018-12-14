package samples.operators

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.util.function.Function

@Suppress("UNCHECKED_CAST")

class BooleanOperatorsTests : StringSpec({
    "and True True returns true" {
        ((file.samples.operators.BooleanOperators.and as Function<Any, Any>).apply(true) as Function<Any, Any>).apply(true)
                .shouldBe(true)
    }

    "and False True returns false" {
        ((file.samples.operators.BooleanOperators.and as Function<Any, Any>).apply(false) as Function<Any, Any>).apply(true)
                .shouldBe(false)
    }

    "and True False returns false" {
        ((file.samples.operators.BooleanOperators.and as Function<Any, Any>).apply(true) as Function<Any, Any>).apply(false)
                .shouldBe(false)
    }

    "and False False returns false" {
        ((file.samples.operators.BooleanOperators.and as Function<Any, Any>).apply(false) as Function<Any, Any>).apply(false)
                .shouldBe(false)
    }


    "or True True returns true" {
        ((file.samples.operators.BooleanOperators.or as Function<Any, Any>).apply(true) as Function<Any, Any>).apply(true)
                .shouldBe(true)
    }

    "or False True returns true" {
        ((file.samples.operators.BooleanOperators.or as Function<Any, Any>).apply(false) as Function<Any, Any>).apply(true)
                .shouldBe(true)
    }

    "or True False returns true" {
        ((file.samples.operators.BooleanOperators.or as Function<Any, Any>).apply(true) as Function<Any, Any>).apply(false)
                .shouldBe(true)
    }

    "or False False returns false" {
        ((file.samples.operators.BooleanOperators.or as Function<Any, Any>).apply(false) as Function<Any, Any>).apply(false)
                .shouldBe(false)
    }
})