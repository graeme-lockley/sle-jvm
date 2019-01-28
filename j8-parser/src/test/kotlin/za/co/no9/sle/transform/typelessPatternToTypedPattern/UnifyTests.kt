package za.co.no9.sle.transform.typelessPatternToTypedPattern

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.QString
import za.co.no9.sle.homeLocation
import za.co.no9.sle.right
import za.co.no9.sle.typing.*


class UnifyTests : StringSpec({
    fun unifiesAsString(constraints: Constraints, environment: Environment = initialEnvironment): String =
            unifies(VarPump(), constraints, environment).right()!!.toString()


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
                        Constraint(TVar(homeLocation, 1), typeInt)))

        unifiesAsString(constraints)
                .shouldBe("'1 Data.Int")
    }


    "constant type and type var result in a single substitution" {
        val constraints =
                Constraints(listOf(
                        Constraint(typeInt, TVar(homeLocation, 1))))

        unifiesAsString(constraints)
                .shouldBe("'1 Data.Int")
    }


    "constant function type and type var result in a single substitution" {
        val constraints =
                Constraints(listOf(
                        Constraint(TArr(typeInt, typeString), TArr(TVar(homeLocation, 2), TVar(homeLocation, 1)))))

        unifiesAsString(constraints)
                .shouldBe("'1 Data.String, '2 Data.Int")
    }

    "missing substitution" {
        val constraints =
                Constraints(listOf(
                        Constraint(
                                TVar(0),
                                TRec(false, listOf(Pair("name", typeString)))),
                        Constraint(
                                TAlias(QString("Person"), emptyList()),
                                TVar(0)),
                        Constraint(
                                TArr(TAlias(QString("Person"), emptyList()), TAlias(QString("Person"), emptyList())),
                                TArr(TVar(0), TAlias(QString("Person"), emptyList()))),
                        Constraint(
                                TVar(1),
                                TRec(false, listOf(Pair("dateOfBirth", TVar(3))))),
                        Constraint(
                                typeInt,
                                TVar(2)),
                        Constraint(
                                TVar(3),
                                TRec(false, listOf(Pair("day", typeInt)))),
                        Constraint(
                                TRec(true, listOf(Pair("day", typeInt), Pair("month", typeInt), Pair("year", typeInt))),
                                TVar(3)),
                        Constraint(
                                TVar(1),
                                TRec(false, listOf(Pair("dateOfBirth", TRec(true, listOf(Pair("day", typeInt), Pair("month", typeInt), Pair("year", typeInt))))))),
                        Constraint(
                                TAlias(QString("Person"), emptyList()),
                                TVar(1)),
                        Constraint(
                                TArr(typeInt, TAlias(QString("Person"), emptyList())),
                                TArr(TVar(2), TAlias(QString("Person"), emptyList()))),
                        Constraint(
                                TArr(TAlias(QString("Person"), emptyList()), TArr(typeInt, TAlias(QString("Person"), emptyList()))),
                                TArr(TVar(1), TArr(typeInt, TAlias(QString("Person"), emptyList()))))
                ))

/*
  '0 : {name : Data.String, ..}
  alias.Person : '0
  alias.Person -> alias.Person : '0 -> alias.Person
  '1 : {dateOfBirth : '3, ..}
  Data.Int : '2
  '3 : {day : Data.Int, ..}
  {day : Data.Int, month : Data.Int, year : Data.Int} : '3
  '1 : {dateOfBirth : {day : Data.Int, month : Data.Int, year : Data.Int}, ..}
  alias.Person : '1
  Data.Int -> alias.Person : '2 -> alias.Person
  alias.Person -> Data.Int -> alias.Person : '1 -> Data.Int -> alias.Person
*/

        val environment =
                initialEnvironment
                        .newType("Person", AliasBinding(Scheme(emptyList(),
                                TRec(true, listOf(Pair("name", typeString), Pair("dateOfBirth", TRec(true, listOf(Pair("day", typeInt), Pair("month", typeInt), Pair("year", typeInt)))))))))

//        println(constraints.leftVar().state.joinToString("\n"))

//        println("---")

//        println(constraints.leftVar().merge(environment).state.joinToString("\n"))

        val message =
                unifies(VarPump(), constraints, environment).right()!!

//        println("---")
//        println(message.state.entries.joinToString("\n"))
    }
})
