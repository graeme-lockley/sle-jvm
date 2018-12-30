package samples

import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldNotBeEmpty
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.util.function.Function

@Suppress("UNCHECKED_CAST")

class ListTests : StringSpec({
    val singletonList =
            ((resource.Prelude.Cons as Function<Any, Any>).apply(1) as Function<Any, Any>).apply(resource.Prelude.Nil)

    val dualList =
            ((resource.Prelude.Cons as Function<Any, Any>).apply(1) as Function<Any, Any>).apply(((resource.Prelude.Cons as Function<Any, Any>).apply(2) as Function<Any, Any>).apply(resource.Prelude.Nil))


    "head [] == None" {
        (file.samples.List.head as Function<Any, Any>).apply(resource.Prelude.Nil)
                .shouldBe(resource.Prelude.None)
    }

    "head [1] == Some 1" {
        (file.samples.List.head as Function<Any, Any>).apply(singletonList)
                .shouldBe((resource.Prelude.Some as Function<Any, Any>).apply(1))
    }

    "tail [1] == Some Nil" {
        (file.samples.List.tail as Function<Any, Any>).apply(singletonList)
                .shouldBe((resource.Prelude.Some as Function<Any, Any>).apply(resource.Prelude.Nil))
    }

    "map plus5 [1, 2] == [6, 7]" {
        val result =
                ((file.samples.List.map as Function<Any, Any>).apply(file.samples.List.plus5) as Function<Any, Any>).apply(dualList)
                        as Array<*>

        listEquals(result, listOf(6, 7))
    }

    "removedups (duplicate (range 1 3)) == [1, 2, 3]" {
        val result =
                (file.samples.List.removedups as Function<Any, Any>).apply((file.samples.List.duplicate as Function<Any, Any>).apply(((file.samples.List.range as Function<Any, Any>).apply(1) as Function<Any, Any>).apply(3)))
                        as Array<*>

        listEquals(result, listOf(1, 2, 3))
    }

    "total == 45" {
        file.samples.List.total.shouldBe(55)
    }
})


fun <T> listEquals(actual: Array<*>, expected: kotlin.collections.List<T>) {
    if ((actual[0] as Int) == resource.Prelude.`Nil$`)
        expected.shouldBeEmpty()
    else {
        expected.shouldNotBeEmpty()
        actual[1].shouldBe(expected[0])
        listEquals(actual[2] as Array<*>, expected.drop(1))
    }
}