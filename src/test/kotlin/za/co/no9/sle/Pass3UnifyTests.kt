package za.co.no9.sle

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.ast.pass3.Constraint
import za.co.no9.sle.ast.pass3.Constraints
import za.co.no9.sle.ast.pass3.unifies


class Pass3UnifyTests : StringSpec({
    "no constraints results in no substitution" {
        val constraints: Constraints =
                listOf()

        unifiesAsString(constraints)
                .shouldBe("")
    }


    "constant types result in no substitution" {
        val constraints =
                listOf(
                        Constraint(typeInt, typeInt),
                        Constraint(typeString, typeString),
                        Constraint(typeBool, typeBool))

        unifiesAsString(constraints)
                .shouldBe("")
    }

    "type var and constant type result in a single substitution" {
        val constraints =
                listOf(
                        Constraint(TVar(1), typeInt))

        unifiesAsString(constraints)
                .shouldBe("'1 Int")
    }

    "constant type and type var result in a single substitution" {
        val constraints =
                listOf(
                        Constraint(typeInt, TVar(1)))

        unifiesAsString(constraints)
                .shouldBe("'1 Int")
    }

    "constant function type and type var result in a single substitution" {
        val constraints =
                listOf(
                        Constraint(TArr(typeInt, typeString), TArr(TVar(2), TVar(1))))

        unifiesAsString(constraints)
                .shouldBe("'1 String, '2 Int")
    }

//    "f:a" {
//        val constraints =
//                listOf(
//                        Pair(TArr(typeInt, TArr(typeInt, typeBool)), TArr(TVar(0), TVar(1))),
//                        Pair(TVar(1), TArr(typeInt, TVar(2))),
//                        Pair(TArr(typeInt, TArr(typeInt, typeInt)), TArr(TVar(0), TVar(3))),
//                        Pair(TArr(typeInt, TArr(typeInt, typeInt)), TArr(TVar(0), TVar(5))),
//                        Pair(TVar(5), TArr(typeInt, TVar(6))),
//                        Pair(TVar(4), TArr(TVar(6), TVar(7))),
//                        Pair(TVar(3), TArr(TVar(7), TVar(8))),
//                        Pair(TVar(2), typeBool),
//                        Pair(typeInt, TVar(8)))
//
//        unifiesAsString(constraints)
//                .shouldBe(listOf("'1 String", "'2 Int"))
//    }
})


fun unifiesAsString(constraints: Constraints): String =
        unifies(constraints).right()!!.toString()
