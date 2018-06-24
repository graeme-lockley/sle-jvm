package za.co.no9.sle

import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.ast.Location
import za.co.no9.sle.ast.Position
import za.co.no9.sle.ast.pass1.BinaryOpExpression
import za.co.no9.sle.ast.pass1.False
import za.co.no9.sle.ast.pass1.NotExpression
import za.co.no9.sle.ast.pass1.True
import za.co.no9.sle.ast.pass2.*


class Pass2Tests : StringSpec({
    val arbLocation =
            Location(Position(1, 2), Position(3, 4))


    val arbPass1Expression =
            True(arbLocation)

    val arbPass2Expression =
            ConstantBool(arbLocation, true)


    "map ID" {
        map(za.co.no9.sle.ast.pass1.ID(arbLocation, "MyName"))
                .shouldBe(ID(arbLocation, "MyName"))
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
            map(za.co.no9.sle.ast.pass1.ConstantInt(arbLocation, n))
                    .shouldBe(ConstantInt(arbLocation, n))
        }
    }


    "map ConstantString expression" {
        assertAll { s: String ->
            map(za.co.no9.sle.ast.pass1.ConstantString(arbLocation, s))
                    .shouldBe(ConstantString(arbLocation, s))
        }
    }


    "map NotExpression" {
        map(NotExpression(arbLocation, arbPass1Expression))
                .shouldBe(CallExpression(arbLocation, IdReference(arbLocation, "(!)"), arbPass2Expression))
    }


    "map IdReference expression" {
        map(za.co.no9.sle.ast.pass1.IdReference(arbLocation, "Hello"))
                .shouldBe(IdReference(arbLocation, "Hello"))
    }


    "map IfExpression " {
        map(za.co.no9.sle.ast.pass1.IfExpression(arbLocation, za.co.no9.sle.ast.pass1.IdReference(arbLocation, "a"), za.co.no9.sle.ast.pass1.IdReference(arbLocation, "b"), za.co.no9.sle.ast.pass1.IdReference(arbLocation, "c")))
                .shouldBe(IfExpression(arbLocation, IdReference(arbLocation, "a"), IdReference(arbLocation, "b"), IdReference(arbLocation, "c")))
    }


    "map LambdaExpression" {
        map(za.co.no9.sle.ast.pass1.LambdaExpression(arbLocation, listOf(za.co.no9.sle.ast.pass1.ID(arbLocation, "a"), za.co.no9.sle.ast.pass1.ID(arbLocation, "b")), za.co.no9.sle.ast.pass1.IdReference(arbLocation, "a")))
                .shouldBe(LambdaExpression(arbLocation, ID(arbLocation, "a"), LambdaExpression(arbLocation, ID(arbLocation, "b"), IdReference(arbLocation, "a"))))
    }


    "map BinaryOpExpression" {
        map(za.co.no9.sle.ast.pass1.BinaryOpExpression(arbLocation, za.co.no9.sle.ast.pass1.IdReference(arbLocation, "a"), za.co.no9.sle.ast.pass1.ID(arbLocation, "+"), za.co.no9.sle.ast.pass1.IdReference(arbLocation, "b")))
                .shouldBe(CallExpression(arbLocation, CallExpression(arbLocation, IdReference(arbLocation, "(+)"), IdReference(arbLocation, "a")), IdReference(arbLocation, "b")))

    }

    "map CallExpression" {
        map(za.co.no9.sle.ast.pass1.CallExpression(arbLocation, za.co.no9.sle.ast.pass1.IdReference(arbLocation, "a"), listOf(za.co.no9.sle.ast.pass1.IdReference(arbLocation, "b"), za.co.no9.sle.ast.pass1.IdReference(arbLocation, "c"))))
                .shouldBe(CallExpression(arbLocation, CallExpression(arbLocation, IdReference(arbLocation, "a"), IdReference(arbLocation, "b")), IdReference(arbLocation, "c")))
    }


    "map module" {
        map(za.co.no9.sle.ast.pass1.Module(arbLocation, listOf(
                za.co.no9.sle.ast.pass1.LetDeclaration(arbLocation, za.co.no9.sle.ast.pass1.ID(arbLocation, "add"), listOf(za.co.no9.sle.ast.pass1.ID(arbLocation, "a"), za.co.no9.sle.ast.pass1.ID(arbLocation, "b")), BinaryOpExpression(arbLocation, za.co.no9.sle.ast.pass1.IdReference(arbLocation, "a"), za.co.no9.sle.ast.pass1.ID(arbLocation, "+"), za.co.no9.sle.ast.pass1.IdReference(arbLocation, "b"))),
                za.co.no9.sle.ast.pass1.LetDeclaration(arbLocation, za.co.no9.sle.ast.pass1.ID(arbLocation, "sub"), listOf(za.co.no9.sle.ast.pass1.ID(arbLocation, "x"), za.co.no9.sle.ast.pass1.ID(arbLocation, "y")), BinaryOpExpression(arbLocation, za.co.no9.sle.ast.pass1.IdReference(arbLocation, "x"), za.co.no9.sle.ast.pass1.ID(arbLocation, "-"), za.co.no9.sle.ast.pass1.IdReference(arbLocation, "y"))))
        )).shouldBe(Module(arbLocation, listOf(
                LetDeclaration(arbLocation, ID(arbLocation, "add"), LambdaExpression(arbLocation, ID(arbLocation, "a"), LambdaExpression(arbLocation, ID(arbLocation, "b"), CallExpression(arbLocation, CallExpression(arbLocation, IdReference(arbLocation, "(+)"), IdReference(arbLocation, "a")), IdReference(arbLocation, "b"))))),
                LetDeclaration(arbLocation, ID(arbLocation, "sub"), LambdaExpression(arbLocation, ID(arbLocation, "x"), LambdaExpression(arbLocation, ID(arbLocation, "y"), CallExpression(arbLocation, CallExpression(arbLocation, IdReference(arbLocation, "(-)"), IdReference(arbLocation, "x")), IdReference(arbLocation, "y")))))
        )))
    }
})
