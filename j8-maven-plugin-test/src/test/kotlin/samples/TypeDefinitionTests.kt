package samples

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.util.function.Function

@Suppress("UNCHECKED_CAST")

class TypeDefinitionTests : StringSpec({
    "singleton 5" {
        (file.samples.TypeDefinition.singleton as Function<Any, Any>).apply(5)
                .shouldBe(arrayOf(file.samples.TypeDefinition.`Cons$`, 5, file.samples.TypeDefinition.Nil))
    }

    "doubleIntList" {
        val result =
                file.samples.TypeDefinition.doubleIntList as Array<*>

        result[0].shouldBe(file.samples.TypeDefinition.`Cons$`)
        result[1].shouldBe(1)
        result[2].shouldBe(arrayOf(file.samples.TypeDefinition.`Cons$`, 2, file.samples.TypeDefinition.Nil))
    }
})