package samples

import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldNotBeEmpty
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class ListTests : StringSpec({
    val singletonList =
            List.Cons.apply(1).apply(List.Nil)

    val dualList =
            List.Cons.apply(1).apply(List.Cons.apply(2).apply(List.Nil))


    "head [] == Nothing" {
        List.head.apply(List.Nil)
                .shouldBe(List.Nothing)
    }

    "head [1] == Just 1" {
        List.head.apply(singletonList)
                .shouldBe(List.Just.apply(1))
    }

    "tail [1] == Just Nil" {
        List.tail.apply(singletonList)
                .shouldBe(List.Just.apply(List.Nil))
    }

    "map plus5 [1, 2] == [6, 7]" {
        val result =
                List.map.apply(List.plus5).apply(dualList)
                        as Array<*>

        listEquals(result, listOf(6, 7))
    }

    "removedups (duplicate (range 1 3)) == [1, 2, 3]" {
        val result =
                List.removedups.apply(List.duplicate.apply(List.range.apply(1).apply(3)))
                        as Array<*>

        listEquals(result, listOf(1, 2, 3))
    }
})


fun <T> listEquals(actual: Array<*>, expected: kotlin.collections.List<T>) {
    if ((actual[0] as Int) == List.`Nil$`)
        expected.shouldBeEmpty()
    else {
        expected.shouldNotBeEmpty()
        actual[1].shouldBe(expected[0])
        listEquals(actual[2] as Array<*>, expected.drop(1))
    }
}