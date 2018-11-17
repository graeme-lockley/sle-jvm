package samples

import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldNotBeEmpty
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class ListTests : StringSpec({
    val singletonList =
            file.samples.List.Cons.apply(1).apply(file.samples.List.Nil)

    val dualList =
            file.samples.List.Cons.apply(1).apply(file.samples.List.Cons.apply(2).apply(file.samples.List.Nil))


    "head [] == Nothing" {
        file.samples.List.head.apply(file.samples.List.Nil)
                .shouldBe(file.samples.List.Nothing)
    }

    "head [1] == Just 1" {
        file.samples.List.head.apply(singletonList)
                .shouldBe(file.samples.List.Just.apply(1))
    }

    "tail [1] == Just Nil" {
        file.samples.List.tail.apply(singletonList)
                .shouldBe(file.samples.List.Just.apply(file.samples.List.Nil))
    }

    "map plus5 [1, 2] == [6, 7]" {
        val result =
                file.samples.List.map.apply(file.samples.List.plus5).apply(dualList)
                        as Array<*>

        listEquals(result, listOf(6, 7))
    }

    "removedups (duplicate (range 1 3)) == [1, 2, 3]" {
        val result =
                file.samples.List.removedups.apply(file.samples.List.duplicate.apply(file.samples.List.range.apply(1).apply(3)))
                        as Array<*>

        listEquals(result, listOf(1, 2, 3))
    }
})


fun <T> listEquals(actual: Array<*>, expected: kotlin.collections.List<T>) {
    if ((actual[0] as Int) == file.samples.List.`Nil$`)
        expected.shouldBeEmpty()
    else {
        expected.shouldNotBeEmpty()
        actual[1].shouldBe(expected[0])
        listEquals(actual[2] as Array<*>, expected.drop(1))
    }
}