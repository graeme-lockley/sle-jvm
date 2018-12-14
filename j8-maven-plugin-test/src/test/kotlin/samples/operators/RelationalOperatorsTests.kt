package samples.operators

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.runtime.Unit
import java.util.function.Function

@Suppress("UNCHECKED_CAST")

class RelationalOperatorsTests : StringSpec({
    "equal () () returns true" {
        ((file.samples.operators.RelationalOperators.equal as Function<Any, Any>).apply(Unit.INSTANCE) as Function<Any, Any>).apply(Unit.INSTANCE)
                .shouldBe(true)
    }

    "notEqual () () returns false" {
        ((file.samples.operators.RelationalOperators.notEqual as Function<Any, Any>).apply(Unit.INSTANCE) as Function<Any, Any>).apply(Unit.INSTANCE)
                .shouldBe(false)
    }

    "less () () returns false" {
        ((file.samples.operators.RelationalOperators.less as Function<Any, Any>).apply(Unit.INSTANCE) as Function<Any, Any>).apply(Unit.INSTANCE)
                .shouldBe(false)
    }

    "lessEqual () () returns true" {
        ((file.samples.operators.RelationalOperators.lessEqual as Function<Any, Any>).apply(Unit.INSTANCE) as Function<Any, Any>).apply(Unit.INSTANCE)
                .shouldBe(true)
    }

    "greater () () returns false" {
        ((file.samples.operators.RelationalOperators.greater as Function<Any, Any>).apply(Unit.INSTANCE) as Function<Any, Any>).apply(Unit.INSTANCE)
                .shouldBe(false)
    }

    "greaterEqual () () returns true" {
        ((file.samples.operators.RelationalOperators.greaterEqual as Function<Any, Any>).apply(Unit.INSTANCE) as Function<Any, Any>).apply(Unit.INSTANCE)
                .shouldBe(true)
    }



    "equal 5 5 returns true" {
        ((file.samples.operators.RelationalOperators.equal as Function<Any, Any>).apply(5) as Function<Any, Any>).apply(5)
                .shouldBe(true)
    }

    "equal 5 6 returns false" {
        ((file.samples.operators.RelationalOperators.equal as Function<Any, Any>).apply(5) as Function<Any, Any>).apply(6)
                .shouldBe(false)
    }

    "notEqual 5 5 returns false" {
        ((file.samples.operators.RelationalOperators.notEqual as Function<Any, Any>).apply(5) as Function<Any, Any>).apply(5)
                .shouldBe(false)
    }

    "notEqual 5 6 returns true" {
        ((file.samples.operators.RelationalOperators.notEqual as Function<Any, Any>).apply(5) as Function<Any, Any>).apply(6)
                .shouldBe(true)
    }

    "less 5 5 returns false" {
        ((file.samples.operators.RelationalOperators.less as Function<Any, Any>).apply(5) as Function<Any, Any>).apply(5)
                .shouldBe(false)
    }

    "less 5 6 returns true" {
        ((file.samples.operators.RelationalOperators.less as Function<Any, Any>).apply(5) as Function<Any, Any>).apply(6)
                .shouldBe(true)
    }

    "lessEqual 5 5 returns true" {
        ((file.samples.operators.RelationalOperators.lessEqual as Function<Any, Any>).apply(5) as Function<Any, Any>).apply(5)
                .shouldBe(true)
    }

    "lessEqual 5 6 returns true" {
        ((file.samples.operators.RelationalOperators.lessEqual as Function<Any, Any>).apply(5) as Function<Any, Any>).apply(6)
                .shouldBe(true)
    }

    "greater 5 5 returns false" {
        ((file.samples.operators.RelationalOperators.greater as Function<Any, Any>).apply(5) as Function<Any, Any>).apply(5)
                .shouldBe(false)
    }

    "greater 5 6 returns false" {
        ((file.samples.operators.RelationalOperators.greater as Function<Any, Any>).apply(5) as Function<Any, Any>).apply(6)
                .shouldBe(false)
    }

    "greaterEqual 5 5 returns true" {
        ((file.samples.operators.RelationalOperators.greaterEqual as Function<Any, Any>).apply(5) as Function<Any, Any>).apply(5)
                .shouldBe(true)
    }

    "greaterEqual 5 6 returns false" {
        ((file.samples.operators.RelationalOperators.greaterEqual as Function<Any, Any>).apply(5) as Function<Any, Any>).apply(6)
                .shouldBe(false)
    }



    "equal \"Hello\" \"Hello\" returns true" {
        ((file.samples.operators.RelationalOperators.equal as Function<Any, Any>).apply("Hello") as Function<Any, Any>).apply("Hello")
                .shouldBe(true)
    }

    "equal \"Hello\" \"World\" returns false" {
        ((file.samples.operators.RelationalOperators.equal as Function<Any, Any>).apply("Hello") as Function<Any, Any>).apply("World")
                .shouldBe(false)
    }

    "notEqual \"Hello\" \"Hello\" returns false" {
        ((file.samples.operators.RelationalOperators.notEqual as Function<Any, Any>).apply("Hello") as Function<Any, Any>).apply("Hello")
                .shouldBe(false)
    }

    "notEqual \"Hello\" \"World\" returns true" {
        ((file.samples.operators.RelationalOperators.notEqual as Function<Any, Any>).apply("Hello") as Function<Any, Any>).apply("World")
                .shouldBe(true)
    }

    "less \"Hello\" \"Hello\" returns false" {
        ((file.samples.operators.RelationalOperators.less as Function<Any, Any>).apply("Hello") as Function<Any, Any>).apply("Hello")
                .shouldBe(false)
    }

    "less \"Hello\" \"World\" returns true" {
        ((file.samples.operators.RelationalOperators.less as Function<Any, Any>).apply("Hello") as Function<Any, Any>).apply("World")
                .shouldBe(true)
    }

    "lessEqual \"Hello\" \"Hello\" returns true" {
        ((file.samples.operators.RelationalOperators.lessEqual as Function<Any, Any>).apply("Hello") as Function<Any, Any>).apply("Hello")
                .shouldBe(true)
    }

    "lessEqual \"Hello\" \"World\" returns true" {
        ((file.samples.operators.RelationalOperators.lessEqual as Function<Any, Any>).apply("Hello") as Function<Any, Any>).apply("World")
                .shouldBe(true)
    }


    "greater \"Hello\" \"Hello\" returns false" {
        ((file.samples.operators.RelationalOperators.greater as Function<Any, Any>).apply("Hello") as Function<Any, Any>).apply("Hello")
                .shouldBe(false)
    }

    "greater \"Hello\" \"World\" returns false" {
        ((file.samples.operators.RelationalOperators.greater as Function<Any, Any>).apply("Hello") as Function<Any, Any>).apply("World")
                .shouldBe(false)
    }

    "greaterEqual \"Hello\" \"Hello\" returns true" {
        ((file.samples.operators.RelationalOperators.greaterEqual as Function<Any, Any>).apply("Hello") as Function<Any, Any>).apply("Hello")
                .shouldBe(true)
    }

    "greaterEqual \"Hello\" \"World\" returns false" {
        ((file.samples.operators.RelationalOperators.greaterEqual as Function<Any, Any>).apply("Hello") as Function<Any, Any>).apply("World")
                .shouldBe(false)
    }



    "equal True True returns true" {
        ((file.samples.operators.RelationalOperators.equal as Function<Any, Any>).apply(true) as Function<Any, Any>).apply(true)
                .shouldBe(true)
    }

    "equal True False returns false" {
        ((file.samples.operators.RelationalOperators.equal as Function<Any, Any>).apply(true) as Function<Any, Any>).apply(false)
                .shouldBe(false)
    }

    "notEqual True True returns false" {
        ((file.samples.operators.RelationalOperators.notEqual as Function<Any, Any>).apply(true) as Function<Any, Any>).apply(true)
                .shouldBe(false)
    }

    "notEqual True False returns true" {
        ((file.samples.operators.RelationalOperators.notEqual as Function<Any, Any>).apply(true) as Function<Any, Any>).apply(false)
                .shouldBe(true)
    }

    "less True True returns false" {
        ((file.samples.operators.RelationalOperators.less as Function<Any, Any>).apply(true) as Function<Any, Any>).apply(true)
                .shouldBe(false)
    }

    "less True False returns false" {
        ((file.samples.operators.RelationalOperators.less as Function<Any, Any>).apply(true) as Function<Any, Any>).apply(false)
                .shouldBe(false)
    }

    "lessEqual True True returns true" {
        ((file.samples.operators.RelationalOperators.lessEqual as Function<Any, Any>).apply(true) as Function<Any, Any>).apply(true)
                .shouldBe(true)
    }

    "lessEqual True False returns false" {
        ((file.samples.operators.RelationalOperators.lessEqual as Function<Any, Any>).apply(true) as Function<Any, Any>).apply(false)
                .shouldBe(false)
    }

    "greater True True returns false" {
        ((file.samples.operators.RelationalOperators.greater as Function<Any, Any>).apply(true) as Function<Any, Any>).apply(true)
                .shouldBe(false)
    }

    "greater True False returns true" {
        ((file.samples.operators.RelationalOperators.greater as Function<Any, Any>).apply(true) as Function<Any, Any>).apply(false)
                .shouldBe(true)
    }

    "greaterEqual True True returns true" {
        ((file.samples.operators.RelationalOperators.greaterEqual as Function<Any, Any>).apply(true) as Function<Any, Any>).apply(true)
                .shouldBe(true)
    }

    "greaterEqual True False returns true" {
        ((file.samples.operators.RelationalOperators.greaterEqual as Function<Any, Any>).apply(true) as Function<Any, Any>).apply(false)
                .shouldBe(true)
    }
})