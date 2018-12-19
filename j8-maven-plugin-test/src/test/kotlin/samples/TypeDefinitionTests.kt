package samples

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.util.function.Function

@Suppress("UNCHECKED_CAST")

class TypeDefinitionTests : StringSpec({
    "singleton 5" {
        (file.samples.TypeDefinition.singleton as Function<Any, Any>).apply(5)
                .shouldBe(arrayOf(resource.Prelude.`Cons$`, 5, resource.Prelude.Nil))
    }

    "doubleIntList" {
        val result =
                file.samples.TypeDefinition.doubleIntList as Array<*>

        result[0].shouldBe(resource.Prelude.`Cons$`)
        result[1].shouldBe(1)
        result[2].shouldBe(arrayOf(resource.Prelude.`Cons$`, 2, resource.Prelude.Nil))
    }
})