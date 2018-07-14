package za.co.no9.sle.pass3

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.*
import za.co.no9.sle.pass1.parseTreeToAST
import za.co.no9.sle.pass2.astToCoreAST
import za.co.no9.sle.typing.*


class InferModuleTests : StringSpec({
    "\"let add a b = a + b\"" {
        val environment =
                Environment(mapOf(
                        Pair("(+)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))))

        val module =
                astToCoreAST(parseTreeToAST(za.co.no9.sle.parser.parseModule("let add a b = a + b").right()!!.node))

        val inferResult =
                infer(VarPump(), module, environment).right()!!

        inferResult.second.toString()
                .shouldBe(
                        "Int -> Int -> Int : '0 -> '2, " +
                                "'2 : '1 -> '3")
    }


    "\"let add a b = a + b\nlet add x y = x + y\"" {
        val environment =
                Environment(mapOf(
                        Pair("(+)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))))

        val module =
                astToCoreAST(parseTreeToAST(za.co.no9.sle.parser.parseModule("let add a b = a + b\nlet add x y = x + y").right()!!.node))

        infer(VarPump(), module, environment)
                .shouldBe(error(listOf(DuplicateLetDeclaration(Location(Position(2, 0), Position(2, 18)), "add"))))
    }


    "\"let add a b : Int -> Int -> Int = something a b\"" {
        val environment =
                Environment(mapOf(
                        Pair("something", Schema(listOf(0), TArr(TVar(0), TArr(TVar(0), TVar(0)))))))

        val module =
                astToCoreAST(parseTreeToAST(za.co.no9.sle.parser.parseModule("let add a b : Int -> Int -> Int = something a b").right()!!.node))

        val inferResult =
                infer(VarPump(), module, environment).right()!!

        inferResult.second.toString()
                .shouldBe(
                        "'0 : '1 -> '3, " +
                                "'2 -> '2 -> '2 : '3 -> '4, " +
                                "Int -> Int -> Int : '0 -> '1 -> '4")
    }


    "\"typealias IntMap = Int -> Int\nlet add a b : IntMap = something a b\" should return a DuplicateLetDeclaration" {
        val environment =
                Environment(mapOf(
                        Pair("something", Schema(listOf(0), TArr(TVar(0), TArr(TVar(0), TVar(0)))))))

        val module =
                astToCoreAST(parseTreeToAST(za.co.no9.sle.parser.parseModule("typealias IntMap = Int -> Int \n" +
                        "let add a b : IntMap = something a b").right()!!.node))

        infer(VarPump(), module, environment).right()!!.second.toString()
                .shouldBe("'0 : '1 -> '3, " +
                        "'2 -> '2 -> '2 : '3 -> '4, " +
                        "IntMap : '0 -> '1 -> '4")
    }


    "\"let add a b = something a b\nlet add a b = something a b\" should return a DuplicateLetDeclaration" {
        val environment =
                Environment(mapOf(
                        Pair("something", Schema(listOf(0), TArr(TVar(0), TArr(TVar(0), TVar(0)))))))

        val module =
                astToCoreAST(parseTreeToAST(za.co.no9.sle.parser.parseModule("let add a b = something a b\n" +
                        "let add a b = something a b").right()!!.node))

        infer(VarPump(), module, environment)
                .shouldBe(error(listOf(DuplicateLetDeclaration(Location(Position(2, 0), Position(2, 26)), "add"))))
    }


    "\"let add a b : Int -> Int = something a b\nlet add a b = something a b\" should return a DuplicateLetDeclaration" {
        val environment =
                Environment(mapOf(
                        Pair("something", Schema(listOf(0), TArr(TVar(0), TArr(TVar(0), TVar(0)))))))

        val module =
                astToCoreAST(parseTreeToAST(za.co.no9.sle.parser.parseModule("let add a b  : Int -> Int = something a b\n" +
                        "let add a b = something a b").right()!!.node))

        infer(VarPump(), module, environment)
                .shouldBe(error(listOf(DuplicateLetDeclaration(Location(Position(2, 0), Position(2, 26)), "add"))))
    }


    "\"let add a b = something a b\nlet add a b : Int -> Int = something a b\" should return a DuplicateLetDeclaration" {
        val environment =
                Environment(mapOf(
                        Pair("something", Schema(listOf(0), TArr(TVar(0), TArr(TVar(0), TVar(0)))))))

        val module =
                astToCoreAST(parseTreeToAST(za.co.no9.sle.parser.parseModule("let add a b = something a b\n" +
                        "let add a b : Int -> Int = something a b").right()!!.node))

        infer(VarPump(), module, environment)
                .shouldBe(error(listOf(DuplicateLetDeclaration(Location(Position(2, 0), Position(2, 39)), "add"))))
    }


    "\"let add a b : Int -> Int = something a b\nlet add a b : Int -> Int = something a b\" should return a DuplicateLetDeclaration" {
        val environment =
                Environment(mapOf(
                        Pair("something", Schema(listOf(0), TArr(TVar(0), TArr(TVar(0), TVar(0)))))))

        val module =
                astToCoreAST(parseTreeToAST(za.co.no9.sle.parser.parseModule("let add a b : Int -> Int = something a b\n" +
                        "let add a b : Int -> Int = something a b").right()!!.node))

        infer(VarPump(), module, environment)
                .shouldBe(error(listOf(DuplicateLetDeclaration(Location(Position(2, 0), Position(2, 39)), "add"))))
    }
})
