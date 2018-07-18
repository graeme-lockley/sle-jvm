package za.co.no9.sle.pass2

import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.Location
import za.co.no9.sle.Position
import za.co.no9.sle.astToCoreAST.ConstantBool
import za.co.no9.sle.astToCoreAST.astToCoreAST
import za.co.no9.sle.parseTreeToASTTranslator.*
import za.co.no9.sle.typing.Schema
import za.co.no9.sle.typing.TArr
import za.co.no9.sle.typing.TCon
import za.co.no9.sle.typing.typeInt


class TransformTests : StringSpec({
    val arbLocation =
            Location(Position(1, 2), Position(3, 4))


    val arbPass1Expression =
            za.co.no9.sle.parseTreeToASTTranslator.True(arbLocation)

    val arbPass2Expression =
            ConstantBool(arbLocation, true)


    "astToCoreAST ID" {
        astToCoreAST(ID(arbLocation, "MyName"))
                .shouldBe(za.co.no9.sle.astToCoreAST.ID(arbLocation, "MyName"))
    }


    "astToCoreAST True expression" {
        astToCoreAST(True(arbLocation))
                .shouldBe(ConstantBool(arbLocation, true))
    }


    "astToCoreAST False expression" {
        astToCoreAST(False(arbLocation))
                .shouldBe(ConstantBool(arbLocation, false))
    }


    "astToCoreAST ConstantInt expression" {
        assertAll { n: Int ->
            astToCoreAST(ConstantInt(arbLocation, n))
                    .shouldBe(za.co.no9.sle.astToCoreAST.ConstantInt(arbLocation, n))
        }
    }


    "astToCoreAST ConstantString expression" {
        assertAll { s: String ->
            astToCoreAST(ConstantString(arbLocation, s))
                    .shouldBe(za.co.no9.sle.astToCoreAST.ConstantString(arbLocation, s))
        }
    }


    "astToCoreAST NotExpression" {
        astToCoreAST(NotExpression(arbLocation, arbPass1Expression))
                .shouldBe(za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "(!)"), arbPass2Expression))
    }


    "astToCoreAST IdReference expression" {
        astToCoreAST(IdReference(arbLocation, "Hello"))
                .shouldBe(za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "Hello"))
    }


    "astToCoreAST IfExpression " {
        astToCoreAST(IfExpression(arbLocation, IdReference(arbLocation, "a"), IdReference(arbLocation, "b"), IdReference(arbLocation, "c")))
                .shouldBe(za.co.no9.sle.astToCoreAST.IfExpression(arbLocation, za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "a"), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "b"), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "c")))
    }


    "astToCoreAST LambdaExpression" {
        astToCoreAST(LambdaExpression(arbLocation, listOf(ID(arbLocation, "a"), ID(arbLocation, "b")), IdReference(arbLocation, "a")))
                .shouldBe(za.co.no9.sle.astToCoreAST.LambdaExpression(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "a"), za.co.no9.sle.astToCoreAST.LambdaExpression(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "b"), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "a"))))
    }


    "astToCoreAST BinaryOpExpression" {
        astToCoreAST(BinaryOpExpression(arbLocation, IdReference(arbLocation, "a"), ID(arbLocation, "+"), IdReference(arbLocation, "b")))
                .shouldBe(za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "(+)"), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "a")), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "b")))

    }

    "astToCoreAST CallExpression" {
        astToCoreAST(CallExpression(arbLocation, IdReference(arbLocation, "a"), listOf(IdReference(arbLocation, "b"), IdReference(arbLocation, "c"))))
                .shouldBe(za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "a"), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "b")), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "c")))
    }


    "astToCoreAST module without type signature" {
        astToCoreAST(Module(arbLocation, listOf(
                LetDeclaration(arbLocation, ID(arbLocation, "add"), listOf(ID(arbLocation, "a"), ID(arbLocation, "b")), null, BinaryOpExpression(arbLocation, IdReference(arbLocation, "a"), ID(arbLocation, "+"), IdReference(arbLocation, "b"))),
                LetDeclaration(arbLocation, ID(arbLocation, "sub"), listOf(ID(arbLocation, "x"), ID(arbLocation, "y")), null, BinaryOpExpression(arbLocation, IdReference(arbLocation, "x"), ID(arbLocation, "-"), IdReference(arbLocation, "y"))))
        )).shouldBe(za.co.no9.sle.astToCoreAST.Module(arbLocation, listOf(
                za.co.no9.sle.astToCoreAST.LetDeclaration(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "add"), null, za.co.no9.sle.astToCoreAST.LambdaExpression(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "a"), za.co.no9.sle.astToCoreAST.LambdaExpression(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "b"), za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "(+)"), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "a")), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "b"))))),
                za.co.no9.sle.astToCoreAST.LetDeclaration(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "sub"), null, za.co.no9.sle.astToCoreAST.LambdaExpression(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "x"), za.co.no9.sle.astToCoreAST.LambdaExpression(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "y"), za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "(-)"), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "x")), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "y")))))
        )))
    }


    "astToCoreAST module with type signature" {
        astToCoreAST(Module(arbLocation, listOf(
                LetDeclaration(arbLocation, ID(arbLocation, "add"), listOf(ID(arbLocation, "a"), ID(arbLocation, "b")), TArrow(arbLocation, TIdReference(arbLocation, "S"), TIdReference(arbLocation, "Int")), BinaryOpExpression(arbLocation, IdReference(arbLocation, "a"), ID(arbLocation, "+"), IdReference(arbLocation, "b"))),
                LetDeclaration(arbLocation, ID(arbLocation, "sub"), listOf(ID(arbLocation, "x"), ID(arbLocation, "y")), TIdReference(arbLocation, "String"), BinaryOpExpression(arbLocation, IdReference(arbLocation, "x"), ID(arbLocation, "-"), IdReference(arbLocation, "y"))))
        )).shouldBe(za.co.no9.sle.astToCoreAST.Module(arbLocation, listOf(
                za.co.no9.sle.astToCoreAST.LetDeclaration(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "add"), Schema(emptyList(), TArr(TCon("S"), TCon("Int"))), za.co.no9.sle.astToCoreAST.LambdaExpression(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "a"), za.co.no9.sle.astToCoreAST.LambdaExpression(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "b"), za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "(+)"), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "a")), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "b"))))),
                za.co.no9.sle.astToCoreAST.LetDeclaration(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "sub"), Schema(emptyList(), TCon("String")), za.co.no9.sle.astToCoreAST.LambdaExpression(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "x"), za.co.no9.sle.astToCoreAST.LambdaExpression(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "y"), za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "(-)"), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "x")), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "y")))))
        )))
    }


    "astToCoreAST module with type alias and let declaration" {
        astToCoreAST(Module(arbLocation, listOf(
                TypeAliasDeclaration(arbLocation, ID(arbLocation, "IntMap"), TArrow(arbLocation, TIdReference(arbLocation, "Int"), TIdReference(arbLocation, "Int"))),
                LetDeclaration(arbLocation, ID(arbLocation, "sub"), listOf(ID(arbLocation, "x"), ID(arbLocation, "y")), TIdReference(arbLocation, "String"), BinaryOpExpression(arbLocation, IdReference(arbLocation, "x"), ID(arbLocation, "-"), IdReference(arbLocation, "y"))))
        )).shouldBe(za.co.no9.sle.astToCoreAST.Module(arbLocation, listOf(
                za.co.no9.sle.astToCoreAST.TypeAliasDeclaration(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "IntMap"), Schema(listOf(), TArr(typeInt, typeInt))),
                za.co.no9.sle.astToCoreAST.LetDeclaration(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "sub"), Schema(emptyList(), TCon("String")), za.co.no9.sle.astToCoreAST.LambdaExpression(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "x"), za.co.no9.sle.astToCoreAST.LambdaExpression(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "y"), za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "(-)"), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "x")), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "y")))))
        )))
    }
})
