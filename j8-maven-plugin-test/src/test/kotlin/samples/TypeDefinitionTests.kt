package samples

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class TypeDefinitionTests : StringSpec({
    "singleton 5" {
        TypeDefinition.singleton.apply(5)
                .shouldBe(arrayOf(TypeDefinition.`Cons$`, 5, TypeDefinition.Nil))
    }

    "doubleIntList" {
        val result =
                TypeDefinition.doubleIntList as Array<*>

        result[0].shouldBe(TypeDefinition.`Cons$`)
        result[1].shouldBe(1)
        result[2].shouldBe(arrayOf(TypeDefinition.`Cons$`, 2, TypeDefinition.Nil))
    }
})