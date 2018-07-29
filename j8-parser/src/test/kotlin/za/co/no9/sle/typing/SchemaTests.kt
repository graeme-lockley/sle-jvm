package za.co.no9.sle.typing

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec


class SchemaTests: StringSpec({
    "given Int should generalist to <> Int" {
        generalise(typeInt).shouldBe(Schema(listOf(), typeInt))
    }

    "given Int -> Int should generalist to <> Int -> Int" {
        generalise(TArr(typeInt, typeInt)).shouldBe(Schema(listOf(), TArr(typeInt, typeInt)))
    }

    "given '1 should generalist to <0> '0" {
        generalise(TVar(1)).shouldBe(Schema(listOf(Parameter(0, null)), TVar(0)))
    }

    "given Int -> '1 should generalist to <0> Int -> '0" {
        generalise(TArr(typeInt, TVar(1))).shouldBe(Schema(listOf(Parameter(0, null)), TArr(typeInt, TVar(0))))
    }
})