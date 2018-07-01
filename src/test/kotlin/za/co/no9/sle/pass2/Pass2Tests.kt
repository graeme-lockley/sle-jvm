package za.co.no9.sle.pass2

import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.Location
import za.co.no9.sle.Position
import za.co.no9.sle.pass1.BinaryOpExpression
import za.co.no9.sle.pass1.False
import za.co.no9.sle.pass1.NotExpression
import za.co.no9.sle.pass1.True
import za.co.no9.sle.pass1.CallExpression
import za.co.no9.sle.pass1.ConstantInt
import za.co.no9.sle.pass1.ConstantString
import za.co.no9.sle.pass1.ID
import za.co.no9.sle.pass1.IdReference
import za.co.no9.sle.pass1.IfExpression
import za.co.no9.sle.pass1.LambdaExpression
import za.co.no9.sle.pass1.LetDeclaration
import za.co.no9.sle.pass1.Module


class Pass2Tests : StringSpec({
    val arbLocation =
            Location(Position(1, 2), Position(3, 4))


    val arbPass1Expression =
            True(arbLocation)

    val arbPass2Expression =
            ConstantBool(arbLocation, true)


    "map ID" {
        map(ID(arbLocation, "MyName"))
                .shouldBe(za.co.no9.sle.pass2.ID(arbLocation, "MyName"))
    }


    "map True expression" {
        map(True(arbLocation))
                .shouldBe(ConstantBool(arbLocation, true))
    }


    "map False expression" {
        map(False(arbLocation))
                .shouldBe(ConstantBool(arbLocation, false))
    }


    "map ConstantInt expression" {
        assertAll { n: Int ->
            map(ConstantInt(arbLocation, n))
                    .shouldBe(za.co.no9.sle.pass2.ConstantInt(arbLocation, n))
        }
    }


    "map ConstantString expression" {
        assertAll { s: String ->
            map(ConstantString(arbLocation, s))
                    .shouldBe(za.co.no9.sle.pass2.ConstantString(arbLocation, s))
        }
    }


    "map NotExpression" {
        map(NotExpression(arbLocation, arbPass1Expression))
                .shouldBe(CallExpression(arbLocation, za.co.no9.sle.pass2.IdReference(arbLocation, "(!)"), arbPass2Expression))
    }


    "map IdReference expression" {
        map(IdReference(arbLocation, "Hello"))
                .shouldBe(za.co.no9.sle.pass2.IdReference(arbLocation, "Hello"))
    }


    "map IfExpression " {
        map(IfExpression(arbLocation, IdReference(arbLocation, "a"), IdReference(arbLocation, "b"), IdReference(arbLocation, "c")))
                .shouldBe(IfExpression(arbLocation, za.co.no9.sle.pass2.IdReference(arbLocation, "a"), za.co.no9.sle.pass2.IdReference(arbLocation, "b"), za.co.no9.sle.pass2.IdReference(arbLocation, "c")))
    }


    "map LambdaExpression" {
        map(LambdaExpression(arbLocation, listOf(ID(arbLocation, "a"), ID(arbLocation, "b")), IdReference(arbLocation, "a")))
                .shouldBe(LambdaExpression(arbLocation, za.co.no9.sle.pass2.ID(arbLocation, "a"), LambdaExpression(arbLocation, za.co.no9.sle.pass2.ID(arbLocation, "b"), za.co.no9.sle.pass2.IdReference(arbLocation, "a"))))
    }


    "map BinaryOpExpression" {
        map(BinaryOpExpression(arbLocation, IdReference(arbLocation, "a"), ID(arbLocation, "+"), IdReference(arbLocation, "b")))
                .shouldBe(CallExpression(arbLocation, CallExpression(arbLocation, za.co.no9.sle.pass2.IdReference(arbLocation, "(+)"), za.co.no9.sle.pass2.IdReference(arbLocation, "a")), za.co.no9.sle.pass2.IdReference(arbLocation, "b")))

    }

    "map CallExpression" {
        map(CallExpression(arbLocation, IdReference(arbLocation, "a"), listOf(IdReference(arbLocation, "b"), IdReference(arbLocation, "c"))))
                .shouldBe(CallExpression(arbLocation, CallExpression(arbLocation, za.co.no9.sle.pass2.IdReference(arbLocation, "a"), za.co.no9.sle.pass2.IdReference(arbLocation, "b")), za.co.no9.sle.pass2.IdReference(arbLocation, "c")))
    }


    "map module" {
        map(Module(arbLocation, listOf(
                LetDeclaration(arbLocation, ID(arbLocation, "add"), listOf(ID(arbLocation, "a"), ID(arbLocation, "b")), BinaryOpExpression(arbLocation, IdReference(arbLocation, "a"), ID(arbLocation, "+"), IdReference(arbLocation, "b"))),
                LetDeclaration(arbLocation, ID(arbLocation, "sub"), listOf(ID(arbLocation, "x"), ID(arbLocation, "y")), BinaryOpExpression(arbLocation, IdReference(arbLocation, "x"), ID(arbLocation, "-"), IdReference(arbLocation, "y"))))
        )).shouldBe(Module(arbLocation, listOf(
                LetDeclaration(arbLocation, za.co.no9.sle.pass2.ID(arbLocation, "add"), LambdaExpression(arbLocation, za.co.no9.sle.pass2.ID(arbLocation, "a"), LambdaExpression(arbLocation, za.co.no9.sle.pass2.ID(arbLocation, "b"), CallExpression(arbLocation, CallExpression(arbLocation, za.co.no9.sle.pass2.IdReference(arbLocation, "(+)"), za.co.no9.sle.pass2.IdReference(arbLocation, "a")), za.co.no9.sle.pass2.IdReference(arbLocation, "b"))))),
                LetDeclaration(arbLocation, za.co.no9.sle.pass2.ID(arbLocation, "sub"), LambdaExpression(arbLocation, za.co.no9.sle.pass2.ID(arbLocation, "x"), LambdaExpression(arbLocation, za.co.no9.sle.pass2.ID(arbLocation, "y"), CallExpression(arbLocation, CallExpression(arbLocation, za.co.no9.sle.pass2.IdReference(arbLocation, "(-)"), za.co.no9.sle.pass2.IdReference(arbLocation, "x")), za.co.no9.sle.pass2.IdReference(arbLocation, "y")))))
        )))
    }
})
