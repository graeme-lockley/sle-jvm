package za.co.no9.sle.pass2

import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.Location
import za.co.no9.sle.Position
import za.co.no9.sle.pass1.*
import za.co.no9.sle.pass1.CallExpression
import za.co.no9.sle.pass1.ConstantInt
import za.co.no9.sle.pass1.ConstantString
import za.co.no9.sle.pass1.ID
import za.co.no9.sle.pass1.IdReference
import za.co.no9.sle.pass1.IfExpression
import za.co.no9.sle.pass1.LambdaExpression
import za.co.no9.sle.pass1.LetDeclaration
import za.co.no9.sle.pass1.Module
import za.co.no9.sle.typing.Schema
import za.co.no9.sle.typing.TArr
import za.co.no9.sle.typing.TCon


class TransformTests : StringSpec({
    val arbLocation =
            Location(Position(1, 2), Position(3, 4))


    val arbPass1Expression =
            True(arbLocation)

    val arbPass2Expression =
            ConstantBool(arbLocation, true)


    "astToCoreAST ID" {
        astToCoreAST(ID(arbLocation, "MyName"))
                .shouldBe(za.co.no9.sle.pass2.ID(arbLocation, "MyName"))
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
                    .shouldBe(za.co.no9.sle.pass2.ConstantInt(arbLocation, n))
        }
    }


    "astToCoreAST ConstantString expression" {
        assertAll { s: String ->
            astToCoreAST(ConstantString(arbLocation, s))
                    .shouldBe(za.co.no9.sle.pass2.ConstantString(arbLocation, s))
        }
    }


    "astToCoreAST NotExpression" {
        astToCoreAST(NotExpression(arbLocation, arbPass1Expression))
                .shouldBe(CallExpression(arbLocation, za.co.no9.sle.pass2.IdReference(arbLocation, "(!)"), arbPass2Expression))
    }


    "astToCoreAST IdReference expression" {
        astToCoreAST(IdReference(arbLocation, "Hello"))
                .shouldBe(za.co.no9.sle.pass2.IdReference(arbLocation, "Hello"))
    }


    "astToCoreAST IfExpression " {
        astToCoreAST(IfExpression(arbLocation, IdReference(arbLocation, "a"), IdReference(arbLocation, "b"), IdReference(arbLocation, "c")))
                .shouldBe(IfExpression(arbLocation, za.co.no9.sle.pass2.IdReference(arbLocation, "a"), za.co.no9.sle.pass2.IdReference(arbLocation, "b"), za.co.no9.sle.pass2.IdReference(arbLocation, "c")))
    }


    "astToCoreAST LambdaExpression" {
        astToCoreAST(LambdaExpression(arbLocation, listOf(ID(arbLocation, "a"), ID(arbLocation, "b")), IdReference(arbLocation, "a")))
                .shouldBe(LambdaExpression(arbLocation, za.co.no9.sle.pass2.ID(arbLocation, "a"), LambdaExpression(arbLocation, za.co.no9.sle.pass2.ID(arbLocation, "b"), za.co.no9.sle.pass2.IdReference(arbLocation, "a"))))
    }


    "astToCoreAST BinaryOpExpression" {
        astToCoreAST(BinaryOpExpression(arbLocation, IdReference(arbLocation, "a"), ID(arbLocation, "+"), IdReference(arbLocation, "b")))
                .shouldBe(CallExpression(arbLocation, CallExpression(arbLocation, za.co.no9.sle.pass2.IdReference(arbLocation, "(+)"), za.co.no9.sle.pass2.IdReference(arbLocation, "a")), za.co.no9.sle.pass2.IdReference(arbLocation, "b")))

    }

    "astToCoreAST CallExpression" {
        astToCoreAST(CallExpression(arbLocation, IdReference(arbLocation, "a"), listOf(IdReference(arbLocation, "b"), IdReference(arbLocation, "c"))))
                .shouldBe(CallExpression(arbLocation, CallExpression(arbLocation, za.co.no9.sle.pass2.IdReference(arbLocation, "a"), za.co.no9.sle.pass2.IdReference(arbLocation, "b")), za.co.no9.sle.pass2.IdReference(arbLocation, "c")))
    }


    "astToCoreAST module without type signature" {
        astToCoreAST(Module(arbLocation, listOf(
                LetDeclaration(arbLocation, ID(arbLocation, "add"), listOf(ID(arbLocation, "a"), ID(arbLocation, "b")), null, BinaryOpExpression(arbLocation, IdReference(arbLocation, "a"), ID(arbLocation, "+"), IdReference(arbLocation, "b"))),
                LetDeclaration(arbLocation, ID(arbLocation, "sub"), listOf(ID(arbLocation, "x"), ID(arbLocation, "y")), null, BinaryOpExpression(arbLocation, IdReference(arbLocation, "x"), ID(arbLocation, "-"), IdReference(arbLocation, "y"))))
        )).shouldBe(Module(arbLocation, listOf(
                LetDeclaration(arbLocation, za.co.no9.sle.pass2.ID(arbLocation, "add"), null, LambdaExpression(arbLocation, za.co.no9.sle.pass2.ID(arbLocation, "a"), LambdaExpression(arbLocation, za.co.no9.sle.pass2.ID(arbLocation, "b"), CallExpression(arbLocation, CallExpression(arbLocation, za.co.no9.sle.pass2.IdReference(arbLocation, "(+)"), za.co.no9.sle.pass2.IdReference(arbLocation, "a")), za.co.no9.sle.pass2.IdReference(arbLocation, "b"))))),
                LetDeclaration(arbLocation, za.co.no9.sle.pass2.ID(arbLocation, "sub"), null, LambdaExpression(arbLocation, za.co.no9.sle.pass2.ID(arbLocation, "x"), LambdaExpression(arbLocation, za.co.no9.sle.pass2.ID(arbLocation, "y"), CallExpression(arbLocation, CallExpression(arbLocation, za.co.no9.sle.pass2.IdReference(arbLocation, "(-)"), za.co.no9.sle.pass2.IdReference(arbLocation, "x")), za.co.no9.sle.pass2.IdReference(arbLocation, "y")))))
        )))
    }


    "astToCoreAST module with type signature" {
        astToCoreAST(Module(arbLocation, listOf(
                LetDeclaration(arbLocation, ID(arbLocation, "add"), listOf(ID(arbLocation, "a"), ID(arbLocation, "b")), za.co.no9.sle.pass1.TArrow(arbLocation, za.co.no9.sle.pass1.TIdReference(arbLocation, "S"), za.co.no9.sle.pass1.TIdReference(arbLocation, "Int")), BinaryOpExpression(arbLocation, IdReference(arbLocation, "a"), ID(arbLocation, "+"), IdReference(arbLocation, "b"))),
                LetDeclaration(arbLocation, ID(arbLocation, "sub"), listOf(ID(arbLocation, "x"), ID(arbLocation, "y")), za.co.no9.sle.pass1.TIdReference(arbLocation, "String"), BinaryOpExpression(arbLocation, IdReference(arbLocation, "x"), ID(arbLocation, "-"), IdReference(arbLocation, "y"))))
        )).shouldBe(Module(arbLocation, listOf(
                LetDeclaration(arbLocation, za.co.no9.sle.pass2.ID(arbLocation, "add"), Schema(emptyList(), TArr(TCon("S"), TCon("Int"))), LambdaExpression(arbLocation, za.co.no9.sle.pass2.ID(arbLocation, "a"), LambdaExpression(arbLocation, za.co.no9.sle.pass2.ID(arbLocation, "b"), CallExpression(arbLocation, CallExpression(arbLocation, za.co.no9.sle.pass2.IdReference(arbLocation, "(+)"), za.co.no9.sle.pass2.IdReference(arbLocation, "a")), za.co.no9.sle.pass2.IdReference(arbLocation, "b"))))),
                LetDeclaration(arbLocation, za.co.no9.sle.pass2.ID(arbLocation, "sub"), Schema(emptyList(), TCon("String")), LambdaExpression(arbLocation, za.co.no9.sle.pass2.ID(arbLocation, "x"), LambdaExpression(arbLocation, za.co.no9.sle.pass2.ID(arbLocation, "y"), CallExpression(arbLocation, CallExpression(arbLocation, za.co.no9.sle.pass2.IdReference(arbLocation, "(-)"), za.co.no9.sle.pass2.IdReference(arbLocation, "x")), za.co.no9.sle.pass2.IdReference(arbLocation, "y")))))
        )))
    }
})
