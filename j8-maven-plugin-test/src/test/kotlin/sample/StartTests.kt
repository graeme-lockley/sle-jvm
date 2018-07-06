package sample

import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.specs.StringSpec

class StartTests : StringSpec({
    "Factorial 5" {
        true.shouldBeTrue()
    }
})