package samples

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec

class FirstTests : StringSpec({
    "Factorial 5 == 120" {
        file.samples.First.factorial.apply(5)
                .shouldBe(120)
    }


    "Factorial 5 == Factorial2 5" {
        file.samples.First.factorial.apply(5)
                .shouldBe(file.samples.First.factorial2.apply(5))
    }


    "Divide 10 2" {
        file.samples.First.divide.apply(10).apply(2)
                .shouldBe(5)
    }


    "Divide 10 0" {
        shouldThrow<ArithmeticException> { file.samples.First.divide.apply(10).apply(0) }
    }
})