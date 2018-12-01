package za.co.no9.sle.typing

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.homeLocation


class SchemeTests : StringSpec({
    "given Int should generalist to <> Int" {
        generalise(typeInt).shouldBe(Scheme(listOf(), typeInt))
    }

    "given Int -> Int should generalist to <> Int -> Int" {
        generalise(TArr(typeInt, typeInt)).shouldBe(Scheme(listOf(), TArr(typeInt, typeInt)))
    }

    "given '1 should generalist to <1> '1" {
        generalise(TVar(homeLocation, 1)).shouldBe(Scheme(listOf(1), TVar(homeLocation, 1)))
    }

    "given Int -> '1 should generalist to <1> Int -> '1" {
        generalise(TArr(typeInt, TVar(homeLocation, 1))).shouldBe(Scheme(listOf(1), TArr(typeInt, TVar(homeLocation, 1))))
    }

    "given '1 and {'1 -> Int} should generalist to <> Int" {
        generalise(TVar(homeLocation, 1), Substitution(mapOf(Pair(1, typeInt)))).shouldBe(Scheme(listOf(), typeInt))
    }

    "given '0 -> '4 and {'0: '3, '1: '3, '2: List '3 -> List '3, '4: List '3} should generalise to <1> '1 -> List '1" {
        generalise(
                TArr(TVar(homeLocation, 0), TVar(homeLocation, 4)),
                Substitution(mapOf(
                        Pair(0, TVar(homeLocation, 3)),
                        Pair(1, TVar(homeLocation, 3)),
                        Pair(2, TArr(TCon(homeLocation, "List", listOf(TVar(homeLocation, 3))), TCon(homeLocation, "List", listOf(TVar(homeLocation, 3))))),
                        Pair(4, TCon(homeLocation, "List", listOf(TVar(homeLocation, 3))))
                ))).shouldBe(Scheme(listOf(3), TArr(TVar(homeLocation, 3), TCon(homeLocation, "List", listOf(TVar(homeLocation, 3))))))
    }

    "given <1> '1 -> '1 should instantiate to " {
        Scheme(listOf(1), TArr(TVar(homeLocation, 1), TVar(homeLocation, 1))).instantiate(VarPump()).shouldBe(TArr(TVar(homeLocation, 0), TVar(homeLocation, 0)))
    }
})