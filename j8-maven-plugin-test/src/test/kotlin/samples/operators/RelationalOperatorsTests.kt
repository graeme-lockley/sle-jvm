package samples.operators

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.runtime.Unit

class RelationalOperatorsTests : StringSpec({
    "equal () () returns true" {
        RelationalOperators.equal.apply(Unit.INSTANCE).apply(Unit.INSTANCE)
                .shouldBe(true)
    }

    "notEqual () () returns false" {
        RelationalOperators.notEqual.apply(Unit.INSTANCE).apply(Unit.INSTANCE)
                .shouldBe(false)
    }

    "less () () returns false" {
        RelationalOperators.less.apply(Unit.INSTANCE).apply(Unit.INSTANCE)
                .shouldBe(false)
    }

    "lessEqual () () returns true" {
        RelationalOperators.lessEqual.apply(Unit.INSTANCE).apply(Unit.INSTANCE)
                .shouldBe(true)
    }

    "greater () () returns false" {
        RelationalOperators.greater.apply(Unit.INSTANCE).apply(Unit.INSTANCE)
                .shouldBe(false)
    }

    "greaterEqual () () returns true" {
        RelationalOperators.greaterEqual.apply(Unit.INSTANCE).apply(Unit.INSTANCE)
                .shouldBe(true)
    }



    "equal 5 5 returns true" {
        RelationalOperators.equal.apply(5).apply(5)
                .shouldBe(true)
    }

    "equal 5 6 returns false" {
        RelationalOperators.equal.apply(5).apply(6)
                .shouldBe(false)
    }

    "notEqual 5 5 returns false" {
        RelationalOperators.notEqual.apply(5).apply(5)
                .shouldBe(false)
    }

    "notEqual 5 6 returns true" {
        RelationalOperators.notEqual.apply(5).apply(6)
                .shouldBe(true)
    }

    "less 5 5 returns false" {
        RelationalOperators.less.apply(5).apply(5)
                .shouldBe(false)
    }

    "less 5 6 returns true" {
        RelationalOperators.less.apply(5).apply(6)
                .shouldBe(true)
    }

    "lessEqual 5 5 returns true" {
        RelationalOperators.lessEqual.apply(5).apply(5)
                .shouldBe(true)
    }

    "lessEqual 5 6 returns true" {
        RelationalOperators.lessEqual.apply(5).apply(6)
                .shouldBe(true)
    }

    "greater 5 5 returns false" {
        RelationalOperators.greater.apply(5).apply(5)
                .shouldBe(false)
    }

    "greater 5 6 returns false" {
        RelationalOperators.greater.apply(5).apply(6)
                .shouldBe(false)
    }

    "greaterEqual 5 5 returns true" {
        RelationalOperators.greaterEqual.apply(5).apply(5)
                .shouldBe(true)
    }

    "greaterEqual 5 6 returns false" {
        RelationalOperators.greaterEqual.apply(5).apply(6)
                .shouldBe(false)
    }



    "equal \"Hello\" \"Hello\" returns true" {
        RelationalOperators.equal.apply("Hello").apply("Hello")
                .shouldBe(true)
    }

    "equal \"Hello\" \"World\" returns false" {
        RelationalOperators.equal.apply("Hello").apply("World")
                .shouldBe(false)
    }

    "notEqual \"Hello\" \"Hello\" returns false" {
        RelationalOperators.notEqual.apply("Hello").apply("Hello")
                .shouldBe(false)
    }

    "notEqual \"Hello\" \"World\" returns true" {
        RelationalOperators.notEqual.apply("Hello").apply("World")
                .shouldBe(true)
    }

    "less \"Hello\" \"Hello\" returns false" {
        RelationalOperators.less.apply("Hello").apply("Hello")
                .shouldBe(false)
    }

    "less \"Hello\" \"World\" returns true" {
        RelationalOperators.less.apply("Hello").apply("World")
                .shouldBe(true)
    }

    "lessEqual \"Hello\" \"Hello\" returns true" {
        RelationalOperators.lessEqual.apply("Hello").apply("Hello")
                .shouldBe(true)
    }

    "lessEqual \"Hello\" \"World\" returns true" {
        RelationalOperators.lessEqual.apply("Hello").apply("World")
                .shouldBe(true)
    }


    "greater \"Hello\" \"Hello\" returns false" {
        RelationalOperators.greater.apply("Hello").apply("Hello")
                .shouldBe(false)
    }

    "greater \"Hello\" \"World\" returns false" {
        RelationalOperators.greater.apply("Hello").apply("World")
                .shouldBe(false)
    }

    "greaterEqual \"Hello\" \"Hello\" returns true" {
        RelationalOperators.greaterEqual.apply("Hello").apply("Hello")
                .shouldBe(true)
    }

    "greaterEqual \"Hello\" \"World\" returns false" {
        RelationalOperators.greaterEqual.apply("Hello").apply("World")
                .shouldBe(false)
    }



    "equal True True returns true" {
        RelationalOperators.equal.apply(true).apply(true)
                .shouldBe(true)
    }

    "equal True False returns false" {
        RelationalOperators.equal.apply(true).apply(false)
                .shouldBe(false)
    }

    "notEqual True True returns false" {
        RelationalOperators.notEqual.apply(true).apply(true)
                .shouldBe(false)
    }

    "notEqual True False returns true" {
        RelationalOperators.notEqual.apply(true).apply(false)
                .shouldBe(true)
    }

    "less True True returns false" {
        RelationalOperators.less.apply(true).apply(true)
                .shouldBe(false)
    }

    "less True False returns false" {
        RelationalOperators.less.apply(true).apply(false)
                .shouldBe(false)
    }

    "lessEqual True True returns true" {
        RelationalOperators.lessEqual.apply(true).apply(true)
                .shouldBe(true)
    }

    "lessEqual True False returns false" {
        RelationalOperators.lessEqual.apply(true).apply(false)
                .shouldBe(false)
    }

    "greater True True returns false" {
        RelationalOperators.greater.apply(true).apply(true)
                .shouldBe(false)
    }

    "greater True False returns true" {
        RelationalOperators.greater.apply(true).apply(false)
                .shouldBe(true)
    }

    "greaterEqual True True returns true" {
        RelationalOperators.greaterEqual.apply(true).apply(true)
                .shouldBe(true)
    }

    "greaterEqual True False returns true" {
        RelationalOperators.greaterEqual.apply(true).apply(false)
                .shouldBe(true)
    }
})