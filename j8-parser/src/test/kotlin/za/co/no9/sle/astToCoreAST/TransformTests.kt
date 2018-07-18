package za.co.no9.sle.astToCoreAST

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.Location
import za.co.no9.sle.Position
import za.co.no9.sle.parseTreeToASTTranslator.*
import za.co.no9.sle.parseTreeToASTTranslator.LetDeclaration
import za.co.no9.sle.parseTreeToASTTranslator.Module
import za.co.no9.sle.parseTreeToASTTranslator.TypeAliasDeclaration
import za.co.no9.sle.typing.Schema
import za.co.no9.sle.typing.TArr
import za.co.no9.sle.typing.TCon
import za.co.no9.sle.typing.typeInt


class TransformTests : StringSpec({
    val arbLocation =
            Location(Position(1, 2), Position(3, 4))


    "astToCoreAST LambdaExpression" {
        astToCoreAST(za.co.no9.sle.parseTreeToASTTranslator.LambdaExpression(arbLocation, listOf(za.co.no9.sle.parseTreeToASTTranslator.ID(arbLocation, "a"), za.co.no9.sle.parseTreeToASTTranslator.ID(arbLocation, "b")), za.co.no9.sle.parseTreeToASTTranslator.IdReference(arbLocation, "a")))
                .shouldBe(za.co.no9.sle.astToCoreAST.LambdaExpression(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "a"), za.co.no9.sle.astToCoreAST.LambdaExpression(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "b"), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "a"))))
    }


    "astToCoreAST BinaryOpExpression" {
        astToCoreAST(BinaryOpExpression(arbLocation, za.co.no9.sle.parseTreeToASTTranslator.IdReference(arbLocation, "a"), za.co.no9.sle.parseTreeToASTTranslator.ID(arbLocation, "+"), za.co.no9.sle.parseTreeToASTTranslator.IdReference(arbLocation, "b")))
                .shouldBe(za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "(+)"), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "a")), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "b")))

    }

    "astToCoreAST CallExpression" {
        astToCoreAST(za.co.no9.sle.parseTreeToASTTranslator.CallExpression(arbLocation, za.co.no9.sle.parseTreeToASTTranslator.IdReference(arbLocation, "a"), listOf(za.co.no9.sle.parseTreeToASTTranslator.IdReference(arbLocation, "b"), za.co.no9.sle.parseTreeToASTTranslator.IdReference(arbLocation, "c"))))
                .shouldBe(za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "a"), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "b")), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "c")))
    }


    "astToCoreAST module without type signature" {
        astToCoreAST(Module(arbLocation, listOf(
                LetDeclaration(arbLocation, za.co.no9.sle.parseTreeToASTTranslator.ID(arbLocation, "add"), listOf(za.co.no9.sle.parseTreeToASTTranslator.ID(arbLocation, "a"), za.co.no9.sle.parseTreeToASTTranslator.ID(arbLocation, "b")), null, BinaryOpExpression(arbLocation, za.co.no9.sle.parseTreeToASTTranslator.IdReference(arbLocation, "a"), za.co.no9.sle.parseTreeToASTTranslator.ID(arbLocation, "+"), za.co.no9.sle.parseTreeToASTTranslator.IdReference(arbLocation, "b"))),
                LetDeclaration(arbLocation, za.co.no9.sle.parseTreeToASTTranslator.ID(arbLocation, "sub"), listOf(za.co.no9.sle.parseTreeToASTTranslator.ID(arbLocation, "x"), za.co.no9.sle.parseTreeToASTTranslator.ID(arbLocation, "y")), null, BinaryOpExpression(arbLocation, za.co.no9.sle.parseTreeToASTTranslator.IdReference(arbLocation, "x"), za.co.no9.sle.parseTreeToASTTranslator.ID(arbLocation, "-"), za.co.no9.sle.parseTreeToASTTranslator.IdReference(arbLocation, "y"))))
        )).shouldBe(za.co.no9.sle.astToCoreAST.Module(arbLocation, listOf(
                za.co.no9.sle.astToCoreAST.LetDeclaration(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "add"), null, za.co.no9.sle.astToCoreAST.LambdaExpression(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "a"), za.co.no9.sle.astToCoreAST.LambdaExpression(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "b"), za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "(+)"), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "a")), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "b"))))),
                za.co.no9.sle.astToCoreAST.LetDeclaration(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "sub"), null, za.co.no9.sle.astToCoreAST.LambdaExpression(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "x"), za.co.no9.sle.astToCoreAST.LambdaExpression(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "y"), za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "(-)"), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "x")), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "y")))))
        )))
    }


    "astToCoreAST module with type signature" {
        astToCoreAST(Module(arbLocation, listOf(
                LetDeclaration(arbLocation, za.co.no9.sle.parseTreeToASTTranslator.ID(arbLocation, "add"), listOf(za.co.no9.sle.parseTreeToASTTranslator.ID(arbLocation, "a"), za.co.no9.sle.parseTreeToASTTranslator.ID(arbLocation, "b")), TArrow(arbLocation, TIdReference(arbLocation, "S"), TIdReference(arbLocation, "Int")), BinaryOpExpression(arbLocation, za.co.no9.sle.parseTreeToASTTranslator.IdReference(arbLocation, "a"), za.co.no9.sle.parseTreeToASTTranslator.ID(arbLocation, "+"), za.co.no9.sle.parseTreeToASTTranslator.IdReference(arbLocation, "b"))),
                LetDeclaration(arbLocation, za.co.no9.sle.parseTreeToASTTranslator.ID(arbLocation, "sub"), listOf(za.co.no9.sle.parseTreeToASTTranslator.ID(arbLocation, "x"), za.co.no9.sle.parseTreeToASTTranslator.ID(arbLocation, "y")), TIdReference(arbLocation, "String"), BinaryOpExpression(arbLocation, za.co.no9.sle.parseTreeToASTTranslator.IdReference(arbLocation, "x"), za.co.no9.sle.parseTreeToASTTranslator.ID(arbLocation, "-"), za.co.no9.sle.parseTreeToASTTranslator.IdReference(arbLocation, "y"))))
        )).shouldBe(za.co.no9.sle.astToCoreAST.Module(arbLocation, listOf(
                za.co.no9.sle.astToCoreAST.LetDeclaration(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "add"), Schema(emptyList(), TArr(TCon("S"), TCon("Int"))), za.co.no9.sle.astToCoreAST.LambdaExpression(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "a"), za.co.no9.sle.astToCoreAST.LambdaExpression(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "b"), za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "(+)"), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "a")), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "b"))))),
                za.co.no9.sle.astToCoreAST.LetDeclaration(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "sub"), Schema(emptyList(), TCon("String")), za.co.no9.sle.astToCoreAST.LambdaExpression(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "x"), za.co.no9.sle.astToCoreAST.LambdaExpression(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "y"), za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "(-)"), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "x")), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "y")))))
        )))
    }


    "astToCoreAST module with type alias and let declaration" {
        astToCoreAST(Module(arbLocation, listOf(
                TypeAliasDeclaration(arbLocation, za.co.no9.sle.parseTreeToASTTranslator.ID(arbLocation, "IntMap"), TArrow(arbLocation, za.co.no9.sle.parseTreeToASTTranslator.TIdReference(arbLocation, "Int"), za.co.no9.sle.parseTreeToASTTranslator.TIdReference(arbLocation, "Int"))),
                LetDeclaration(arbLocation, za.co.no9.sle.parseTreeToASTTranslator.ID(arbLocation, "sub"), listOf(za.co.no9.sle.parseTreeToASTTranslator.ID(arbLocation, "x"), za.co.no9.sle.parseTreeToASTTranslator.ID(arbLocation, "y")), TIdReference(arbLocation, "String"), BinaryOpExpression(arbLocation, za.co.no9.sle.parseTreeToASTTranslator.IdReference(arbLocation, "x"), za.co.no9.sle.parseTreeToASTTranslator.ID(arbLocation, "-"), za.co.no9.sle.parseTreeToASTTranslator.IdReference(arbLocation, "y"))))
        )).shouldBe(za.co.no9.sle.astToCoreAST.Module(arbLocation, listOf(
                za.co.no9.sle.astToCoreAST.TypeAliasDeclaration(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "IntMap"), Schema(listOf(), TArr(typeInt, typeInt))),
                za.co.no9.sle.astToCoreAST.LetDeclaration(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "sub"), Schema(emptyList(), TCon("String")), za.co.no9.sle.astToCoreAST.LambdaExpression(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "x"), za.co.no9.sle.astToCoreAST.LambdaExpression(arbLocation, za.co.no9.sle.astToCoreAST.ID(arbLocation, "y"), za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.CallExpression(arbLocation, za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "(-)"), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "x")), za.co.no9.sle.astToCoreAST.IdReference(arbLocation, "y")))))
        )))
    }
})
