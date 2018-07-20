package za.co.no9.sle.inference

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.*
import za.co.no9.sle.parseTreeToASTTranslator.parse
import za.co.no9.sle.astToCoreAST.astToCoreAST
import za.co.no9.sle.typing.*


class InferModuleTests : StringSpec({

    fun inferModuleFromText(input: String, environment: Environment) =
            infer(VarPump(), astToCoreAST(parse(input).right()!!), environment)


    "\"let add a b : Int -> Int -> Int = something a b\"" {
        val environment =
                Environment(mapOf(
                        Pair("something", Schema(listOf(0), TArr(TVar(0), TArr(TVar(0), TVar(0)))))))

        inferModuleFromText("let add a b : Int -> Int -> Int = something a b", environment).right()!!.second.toString()
                .shouldBe(
                        "'0 : '1 -> '3, " +
                                "'2 -> '2 -> '2 : '3 -> '4, " +
                                "Int -> Int -> Int : '0 -> '1 -> '4")
    }


    "\"typealias IntMap = Int -> Int\nlet add a b : IntMap = something a b\" should return a DuplicateLetDeclaration" {
        val environment =
                Environment(mapOf(
                        Pair("something", Schema(listOf(0), TArr(TVar(0), TArr(TVar(0), TVar(0)))))))

        inferModuleFromText("typealias IntMap = Int -> Int \n" +
                "let add a b : IntMap = something a b", environment).right()!!.second.toString()
                .shouldBe("'0 : '1 -> '3, " +
                        "'2 -> '2 -> '2 : '3 -> '4, " +
                        "IntMap : '0 -> '1 -> '4")
    }


    "\"let add a b = something a b\nlet add a b = something a b\" should return a DuplicateLetDeclaration" {
        val environment =
                Environment(mapOf(
                        Pair("something", Schema(listOf(0), TArr(TVar(0), TArr(TVar(0), TVar(0)))))))

        inferModuleFromText("let add a b = something a b\n" +
                "let add a b = something a b", environment)
                .shouldBe(error(listOf(DuplicateLetDeclaration(Location(Position(2, 0), Position(2, 26)), "add"))))
    }


    "\"let add a b : Int -> Int = something a b\nlet add a b = something a b\" should return a DuplicateLetDeclaration" {
        val environment =
                Environment(mapOf(
                        Pair("something", Schema(listOf(0), TArr(TVar(0), TArr(TVar(0), TVar(0)))))))

        inferModuleFromText("let add a b  : Int -> Int = something a b\n" +
                "let add a b = something a b", environment)
                .shouldBe(error(listOf(DuplicateLetDeclaration(Location(Position(2, 0), Position(2, 26)), "add"))))
    }


    "\"let add a b = something a b\nlet add a b : Int -> Int = something a b\" should return a DuplicateLetDeclaration" {
        val environment =
                Environment(mapOf(
                        Pair("something", Schema(listOf(0), TArr(TVar(0), TArr(TVar(0), TVar(0)))))))

        inferModuleFromText("let add a b = something a b\n" +
                "let add a b : Int -> Int = something a b", environment)
                .shouldBe(error(listOf(DuplicateLetDeclaration(Location(Position(2, 0), Position(2, 39)), "add"))))
    }


    "\"let add a b : Int -> Int = something a b\nlet add a b : Int -> Int = something a b\" should return a DuplicateLetDeclaration" {
        val environment =
                Environment(mapOf(
                        Pair("something", Schema(listOf(0), TArr(TVar(0), TArr(TVar(0), TVar(0)))))))

        inferModuleFromText("let add a b : Int -> Int = something a b\n" +
                "let add a b : Int -> Int = something a b", environment)
                .shouldBe(error(listOf(DuplicateLetDeclaration(Location(Position(2, 0), Position(2, 39)), "add"))))
    }
})
