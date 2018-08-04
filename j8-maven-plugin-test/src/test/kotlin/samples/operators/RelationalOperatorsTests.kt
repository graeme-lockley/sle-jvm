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

    "equal True True returns true" {
        RelationalOperators.equal.apply(true).apply(true)
                .shouldBeTrue()
    }

    "equal True False returns false" {
        RelationalOperators.equal.apply(true).apply(false)
                .shouldBeFalse()
    }
})