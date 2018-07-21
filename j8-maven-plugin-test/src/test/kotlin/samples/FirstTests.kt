package samples

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec

class FirstTests : StringSpec({
    "Factorial 5" {
        First.factorial.apply(5)
                .shouldBe(120)
    }


    "Divide 10 2" {
        First.divide.apply(10).apply(2)
                .shouldBe(5)
    }


    "Divide 10 0" {
        shouldThrow<ArithmeticException> { First.divide.apply(10).apply(0) }
    }
})