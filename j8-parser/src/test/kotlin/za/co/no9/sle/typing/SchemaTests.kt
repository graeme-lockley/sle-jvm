package za.co.no9.sle.typing

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec


class SchemaTests : StringSpec({
    "given Int should generalist to <> Int" {
        generalise(typeInt).shouldBe(Schema(listOf(), typeInt))
    }

    "given Int -> Int should generalist to <> Int -> Int" {
        generalise(TArr(typeInt, typeInt)).shouldBe(Schema(listOf(), TArr(typeInt, typeInt)))
    }

    "given '1 should generalist to <1> '1" {
        generalise(TVar(1)).shouldBe(Schema(listOf(Parameter(1, null)), TVar(1)))
    }

    "given Int -> '1 should generalist to <1> Int -> '1" {
        generalise(TArr(typeInt, TVar(1))).shouldBe(Schema(listOf(Parameter(1, null)), TArr(typeInt, TVar(1))))
    }

    "given '1 and {'1 -> Int} should generalist to <> Int" {
        generalise(TVar(1), Substitution(mapOf(Pair(1, typeInt)))).shouldBe(Schema(listOf(), typeInt))
    }
})