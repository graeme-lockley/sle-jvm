package za.co.no9.sle.typing

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec


class SchemeTests : StringSpec({
    "given Int should generalist to <> Int" {
        generalise(typeInt).shouldBe(Scheme(listOf(), typeInt))
    }

    "given Int -> Int should generalist to <> Int -> Int" {
        generalise(TArr(typeInt, typeInt)).shouldBe(Scheme(listOf(), TArr(typeInt, typeInt)))
    }

    "given '1 should generalist to <1> '1" {
        generalise(TVar(1)).shouldBe(Scheme(listOf(1), TVar(1)))
    }

    "given Int -> '1 should generalist to <1> Int -> '1" {
        generalise(TArr(typeInt, TVar(1))).shouldBe(Scheme(listOf(1), TArr(typeInt, TVar(1))))
    }

    "given '1 and {'1 -> Int} should generalist to <> Int" {
        generalise(TVar(1), Substitution(mapOf(Pair(1, typeInt)))).shouldBe(Scheme(listOf(), typeInt))
    }

    "given <1> '1 -> '1 should instantiate to " {
        Scheme(listOf(1), TArr(TVar(1), TVar(1))).instantiate(VarPump()).shouldBe(TArr(TVar(0), TVar(0)))
    }
})