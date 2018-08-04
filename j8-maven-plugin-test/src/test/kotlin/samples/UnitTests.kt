package samples

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec

class UnitTests : StringSpec({
    "value returns 100" {
        Unit.value
                .shouldBe(100)
    }


    "calculate () returns 100" {
        Unit.calculate.apply(za.co.no9.sle.runtime.Unit.INSTANCE)
                .shouldBe(100)
    }
})