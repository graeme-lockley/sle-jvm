package za.co.no9.sle.transform.typelessPatternToTypedPattern

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.typing.TRec
import za.co.no9.sle.typing.TVar
import za.co.no9.sle.typing.typeInt
import za.co.no9.sle.typing.typeString


class ConstraintMergeTests : StringSpec({
    "leftVar of constraints" {
        val constraints =
                Constraints(listOf(
                        Constraint(TVar(1), typeInt),
                        Constraint(typeString, TVar(2)),
                        Constraint(TVar(3), TVar(4))))

        constraints.leftVar()
                .shouldBe(
                        Constraints(listOf(
                                Constraint(TVar(1), typeInt),
                                Constraint(TVar(2), typeString),
                                Constraint(TVar(3), TVar(4))))
                )
    }


    "merge of two open empty records" {
        val constraints =
                Constraints(listOf(
                        Constraint(TVar(1), TRec(false, listOf())),
                        Constraint(TVar(2), typeString),
                        Constraint(TVar(1), TRec(false, listOf())),
                        Constraint(TVar(3), typeString)))

        constraints.merge()
                .shouldBe(
                        Constraints(listOf(
                                Constraint(TVar(1), TRec(false, listOf())),
                                Constraint(TVar(2), typeString),
                                Constraint(TVar(3), typeString)))
                )
    }


    "merge of two open records with a single common field" {
        val constraints =
                Constraints(listOf(
                        Constraint(TVar(1), TRec(false, listOf(Pair("name", typeString)))),
                        Constraint(TVar(2), typeString),
                        Constraint(TVar(1), TRec(false, listOf(Pair("name", typeString)))),
                        Constraint(TVar(3), typeString)))

        constraints.merge()
                .shouldBe(
                        Constraints(listOf(
                                Constraint(TVar(1), TRec(false, listOf(Pair("name", typeString)))),
                                Constraint(TVar(2), typeString),
                                Constraint(TVar(3), typeString)))
                )
    }


    "merge of two open records with differing single fields" {
        val constraints =
                Constraints(listOf(
                        Constraint(TVar(1), TRec(false, listOf(Pair("firstName", typeString)))),
                        Constraint(TVar(2), typeString),
                        Constraint(TVar(1), TRec(false, listOf(Pair("surname", typeString)))),
                        Constraint(TVar(3), typeString)))

        constraints.merge()
                .shouldBe(
                        Constraints(listOf(
                                Constraint(TVar(1), TRec(false, listOf(Pair("firstName", typeString), Pair("surname", typeString)))),
                                Constraint(TVar(2), typeString),
                                Constraint(TVar(3), typeString)))
                )
    }


    "merge of two open records with differing overlapping fields" {
        val constraints =
                Constraints(listOf(
                        Constraint(TVar(1), TRec(false, listOf(Pair("name", TVar(2))))),
                        Constraint(TVar(2), typeString),
                        Constraint(TVar(1), TRec(false, listOf(Pair("name", TVar(3))))),
                        Constraint(TVar(3), typeString)))

        constraints.merge()
                .shouldBe(
                        Constraints(listOf(
                                Constraint(TVar(1), TRec(false, listOf(Pair("name", TVar(2))))),
                                Constraint(TVar(2), typeString)))
                )
    }
})
