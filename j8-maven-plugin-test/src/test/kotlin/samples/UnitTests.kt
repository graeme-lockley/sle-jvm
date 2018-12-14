package samples

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.util.function.Function

@Suppress("UNCHECKED_CAST")

class UnitTests : StringSpec({
    "value returns 100" {
        file.samples.Unit.value
                .shouldBe(100)
    }


    "calculate () returns 100" {
        (file.samples.Unit.calculate as Function<Any, Any>).apply(za.co.no9.sle.runtime.Unit.INSTANCE)
                .shouldBe(100)
    }
})