package samples.operators

import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.runtime.Unit

class RelationalOperatorsTests : StringSpec({
    "equal () () returns true" {
        RelationalOperators.equal.apply(Unit.INSTANCE).apply(Unit.INSTANCE)
                .shouldBeTrue()
    }

    "notEqual () () returns false" {
        RelationalOperators.notEqual.apply(Unit.INSTANCE).apply(Unit.INSTANCE)
                .shouldBeFalse()
    }

    "less () () returns false" {
        RelationalOperators.less.apply(Unit.INSTANCE).apply(Unit.INSTANCE)
                .shouldBeFalse()
    }

    "lessEqual () () returns true" {
        RelationalOperators.lessEqual.apply(Unit.INSTANCE).apply(Unit.INSTANCE)
                .shouldBeTrue()
    }

    "greater () () returns false" {
        RelationalOperators.greater.apply(Unit.INSTANCE).apply(Unit.INSTANCE)
                .shouldBeFalse()
    }

    "greaterEqual () () returns true" {
        RelationalOperators.greaterEqual.apply(Unit.INSTANCE).apply(Unit.INSTANCE)
                .shouldBeTrue()
    }



    "equal 5 5 returns true" {
        RelationalOperators.equal.apply(5).apply(5)
                .shouldBeTrue()
    }

    "equal 5 6 returns false" {
        RelationalOperators.equal.apply(5).apply(6)
                .shouldBeFalse()
    }

    "notEqual 5 5 returns false" {
        RelationalOperators.notEqual.apply(5).apply(5)
                .shouldBeFalse()
    }

    "notEqual 5 6 returns true" {
        RelationalOperators.notEqual.apply(5).apply(6)
                .shouldBeTrue()
    }

    "less 5 5 returns false" {
        RelationalOperators.less.apply(5).apply(5)
                .shouldBeFalse()
    }

    "less 5 6 returns true" {
        RelationalOperators.less.apply(5).apply(6)
                .shouldBeTrue()
    }

    "lessEqual 5 5 returns true" {
        RelationalOperators.lessEqual.apply(5).apply(5)
                .shouldBeTrue()
    }

    "lessEqual 5 6 returns true" {
        RelationalOperators.lessEqual.apply(5).apply(6)
                .shouldBeTrue()
    }

    "greater 5 5 returns false" {
        RelationalOperators.greater.apply(5).apply(5)
                .shouldBeFalse()
    }

    "greater 5 6 returns false" {
        RelationalOperators.greater.apply(5).apply(6)
                .shouldBeFalse()
    }

    "greaterEqual 5 5 returns true" {
        RelationalOperators.greaterEqual.apply(5).apply(5)
                .shouldBeTrue()
    }

    "greaterEqual 5 6 returns false" {
        RelationalOperators.greaterEqual.apply(5).apply(6)
                .shouldBeFalse()
    }



    "equal \"Hello\" \"Hello\" returns true" {
        RelationalOperators.equal.apply("Hello").apply("Hello")
                .shouldBeTrue()
    }

    "equal \"Hello\" \"World\" returns false" {
        RelationalOperators.equal.apply("Hello").apply("World")
                .shouldBeFalse()
    }

    "notEqual \"Hello\" \"Hello\" returns false" {
        RelationalOperators.notEqual.apply("Hello").apply("Hello")
                .shouldBeFalse()
    }

    "notEqual \"Hello\" \"World\" returns true" {
        RelationalOperators.notEqual.apply("Hello").apply("World")
                .shouldBeTrue()
    }

    "less \"Hello\" \"Hello\" returns false" {
        RelationalOperators.less.apply("Hello").apply("Hello")
                .shouldBeFalse()
    }

    "less \"Hello\" \"World\" returns true" {
        RelationalOperators.less.apply("Hello").apply("World")
                .shouldBeTrue()
    }

    "lessEqual \"Hello\" \"Hello\" returns true" {
        RelationalOperators.lessEqual.apply("Hello").apply("Hello")
                .shouldBeTrue()
    }

    "lessEqual \"Hello\" \"World\" returns true" {
        RelationalOperators.lessEqual.apply("Hello").apply("World")
                .shouldBeTrue()
    }


    "greater \"Hello\" \"Hello\" returns false" {
        RelationalOperators.greater.apply("Hello").apply("Hello")
                .shouldBeFalse()
    }

    "greater \"Hello\" \"World\" returns false" {
        RelationalOperators.greater.apply("Hello").apply("World")
                .shouldBeFalse()
    }

    "greaterEqual \"Hello\" \"Hello\" returns true" {
        RelationalOperators.greaterEqual.apply("Hello").apply("Hello")
                .shouldBeTrue()
    }

    "greaterEqual \"Hello\" \"World\" returns false" {
        RelationalOperators.greaterEqual.apply("Hello").apply("World")
                .shouldBeFalse()
    }



    "equal True True returns true" {
        RelationalOperators.equal.apply(true).apply(true)
                .shouldBeTrue()
    }

    "equal True False returns false" {
        RelationalOperators.equal.apply(true).apply(false)
                .shouldBeFalse()
    }

    "notEqual True True returns false" {
        RelationalOperators.notEqual.apply(true).apply(true)
                .shouldBeFalse()
    }

    "notEqual True False returns true" {
        RelationalOperators.notEqual.apply(true).apply(false)
                .shouldBeTrue()
    }

    "less True True returns false" {
        RelationalOperators.less.apply(true).apply(true)
                .shouldBeFalse()
    }

    "less True False returns false" {
        RelationalOperators.less.apply(true).apply(false)
                .shouldBeFalse()
    }

    "lessEqual True True returns true" {
        RelationalOperators.lessEqual.apply(true).apply(true)
                .shouldBeTrue()
    }

    "lessEqual True False returns false" {
        RelationalOperators.lessEqual.apply(true).apply(false)
                .shouldBeFalse()
    }

    "greater True True returns false" {
        RelationalOperators.greater.apply(true).apply(true)
                .shouldBeFalse()
    }

    "greater True False returns true" {
        RelationalOperators.greater.apply(true).apply(false)
                .shouldBeTrue()
    }

    "greaterEqual True True returns true" {
        RelationalOperators.greaterEqual.apply(true).apply(true)
                .shouldBeTrue()
    }

    "greaterEqual True False returns true" {
        RelationalOperators.greaterEqual.apply(true).apply(false)
                .shouldBeTrue()
    }
})