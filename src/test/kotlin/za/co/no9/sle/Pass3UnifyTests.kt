package za.co.no9.sle

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.ast.pass3.Constraints
import za.co.no9.sle.ast.pass3.unifies


class Pass3UnifyTests : StringSpec({
    "no constraints results in no substitution" {
        val constraints: Constraints =
                listOf()

        unifiesAsString(constraints)
                .shouldBe(listOf())
    }


    "constant types result in no substitution" {
        val constraints =
                listOf(
                        Pair(typeInt, typeInt),
                        Pair(typeString, typeString),
                        Pair(typeBool, typeBool))

        unifiesAsString(constraints)
                .shouldBe(listOf())
    }

    "type var and constant type result in a single substitution" {
        val constraints =
                listOf(
                        Pair(TVar(1), typeInt))

        unifiesAsString(constraints)
                .shouldBe(listOf("'1 Int"))
    }

    "constant type and type var result in a single substitution" {
        val constraints =
                listOf(
                        Pair(typeInt, TVar(1)))

        unifiesAsString(constraints)
                .shouldBe(listOf("'1 Int"))
    }

    "constant function type and type var result in a single substitution" {
        val constraints =
                listOf(
                        Pair(TArr(typeInt, typeString), TArr(TVar(2), TVar(1))))

        unifiesAsString(constraints)
                .shouldBe(listOf("'1 String", "'2 Int"))
    }
})


fun unifiesAsString(constraints: Constraints): List<String> =
        unifies(constraints).right()!!.asString()


fun Subst.asString(): List<String> =
        this.entries.map { "'${it.key} ${it.value}" }.sorted()