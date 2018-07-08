package samples

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class FirstTests : StringSpec({
    "Factorial 5" {
        First.factorial.apply(5)
                .shouldBe(120)
    }
})