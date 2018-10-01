package za.co.no9.sle.transform.typelessPatternToTypedCore

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.*
import za.co.no9.sle.typing.*


class UnifyTests : StringSpec({
    fun unifiesAsString(constraints: Constraints): String =
            unifies(VarPump(), emptyMap(), constraints).right()!!.toString()


    "no constraints results in no substitution" {
        val constraints: Constraints =
                noConstraints

        unifiesAsString(constraints)
                .shouldBe("")
    }


    "constant types result in no substitution" {
        val constraints =
                Constraints(listOf(
                        Constraint(typeInt, typeInt),
                        Constraint(typeString, typeString),
                        Constraint(typeBool, typeBool)))

        unifiesAsString(constraints)
                .shouldBe("")
    }


    "type var and constant type result in a single substitution" {
        val constraints =
                Constraints(listOf(
                        Constraint(TVar(1), typeInt)))

        unifiesAsString(constraints)
                .shouldBe("'1 Int")
    }


    "constant type and type var result in a single substitution" {
        val constraints =
                Constraints(listOf(
                        Constraint(typeInt, TVar(1))))

        unifiesAsString(constraints)
                .shouldBe("'1 Int")
    }


    "constant function type and type var result in a single substitution" {
        val constraints =
                Constraints(listOf(
                        Constraint(TArr(typeInt, typeString), TArr(TVar(2), TVar(1)))))

        unifiesAsString(constraints)
                .shouldBe("'1 String, '2 Int")
    }
})
