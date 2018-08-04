package samples.operators

import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.specs.StringSpec

class RelationalOperatorsTests : StringSpec({
    "5 == 5 - true" {
        RelationalOperators.equal.apply(5).apply(5)
                .shouldBeTrue()
    }

    "5 == 6 - false" {
        RelationalOperators.equal.apply(5).apply(6)
                .shouldBeFalse()
    }
})