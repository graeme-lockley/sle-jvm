package samples

import file.samples.First
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import java.util.function.Function

@Suppress("UNCHECKED_CAST")

class FirstTests : StringSpec({
    "Factorial 5 == 120" {
        (First.factorial as Function<Any, Any>).apply(5)
                .shouldBe(120)
    }


    "Factorial 5 == Factorial2 5" {
        (file.samples.First.factorial as Function<Any, Any>).apply(5)
                .shouldBe((file.samples.First.factorial2 as Function<Any, Any>).apply(5))
    }


    "Divide 10 2" {
        ((file.samples.First.divide as Function<Any, Any>).apply(10) as Function<Int, Int>).apply(2)
                .shouldBe(5)
    }


    "Divide 10 0" {
        shouldThrow<ArithmeticException> { ((file.samples.First.divide as Function<Any, Any>).apply(10) as Function<Any, Any>).apply(0) }
    }

    "maxInt" {
        file.samples.First.maxInt
                .shouldBe(Int.MAX_VALUE)
    }

    "minInt" {
        file.samples.First.minInt
                .shouldBe(Int.MIN_VALUE)
    }
})