package samples

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class ListTests : StringSpec({
    val emptyList =
            List.Nil

    val singletonList =
            List.Cons.apply(1).apply(List.Nil)

    val dualList =
            List.Cons.apply(1).apply(List.Cons.apply(2).apply(List.Nil))


    "head [] == Nothing" {
        List.head.apply(emptyList)
                .shouldBe(List.Nothing)
    }

    "head [1] == Just 1" {
        List.head.apply(singletonList)
                .shouldBe(List.Just.apply(1))
    }

    "tail [1] == Just Nil" {
        List.tail.apply(singletonList)
                .shouldBe(List.Just.apply(emptyList))
    }

    "map plus5 [1, 2] == [6, 7]" {
        val result =
                List.map.apply(List.plus5).apply(dualList) as Array<*>

        result[0].shouldBe(TypeDefinition.`Cons$`)
        result[1].shouldBe(6)
        result[2].shouldBe(arrayOf(List.`Cons$`, 7, List.Nil))
    }
})