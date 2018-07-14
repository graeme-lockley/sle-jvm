package za.co.no9.sle.pass1

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.Either
import za.co.no9.sle.parser.Result
import za.co.no9.sle.parser.parseModule
import za.co.no9.sle.right


class ModuleTests : StringSpec({
    fun parse(input: String, output: String) {
        val parseResult =
                parseModule(input)

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        parseTreeToAST(parseResult.right()!!.node).toString().shouldBe(output)
    }


    "\"let add x y : Int -> Int -> Int = x + y\" should produce AST LetDeclaration" {
        parse(
                "let add x y : Int -> Int -> Int = x + y",
                "Module(location=[(1, 0) (1, 38)], declarations=[LetDeclaration(location=[(1, 0) (1, 38)], name=ID(location=[(1, 4) (1, 6)], name=add), arguments=[ID(location=[(1, 8) (1, 8)], name=x), ID(location=[(1, 10) (1, 10)], name=y)], schema=TArrow(location=[(1, 14) (1, 30)], domain=TIdReference(location=[(1, 14) (1, 16)], name=Int), range=TArrow(location=[(1, 21) (1, 30)], domain=TIdReference(location=[(1, 21) (1, 23)], name=Int), range=TIdReference(location=[(1, 28) (1, 30)], name=Int))), expression=BinaryOpExpression(location=[(1, 34) (1, 38)], left=IdReference(location=[(1, 34) (1, 34)], name=x), operator=ID(location=[(1, 36) (1, 36)], name=+), right=IdReference(location=[(1, 38) (1, 38)], name=y)))])"
        )
    }


    "\"let add x y = x + y\nlet sub a b = a - b\" should produce AST Module" {
        parse(
                "let add x y = x + y\n" +
                        "let sub a b = a - b",
                "Module(location=[(1, 0) (2, 18)], declarations=[LetDeclaration(location=[(1, 0) (1, 18)], name=ID(location=[(1, 4) (1, 6)], name=add), arguments=[ID(location=[(1, 8) (1, 8)], name=x), ID(location=[(1, 10) (1, 10)], name=y)], schema=null, expression=BinaryOpExpression(location=[(1, 14) (1, 18)], left=IdReference(location=[(1, 14) (1, 14)], name=x), operator=ID(location=[(1, 16) (1, 16)], name=+), right=IdReference(location=[(1, 18) (1, 18)], name=y))), LetDeclaration(location=[(2, 0) (2, 18)], name=ID(location=[(2, 4) (2, 6)], name=sub), arguments=[ID(location=[(2, 8) (2, 8)], name=a), ID(location=[(2, 10) (2, 10)], name=b)], schema=null, expression=BinaryOpExpression(location=[(2, 14) (2, 18)], left=IdReference(location=[(2, 14) (2, 14)], name=a), operator=ID(location=[(2, 16) (2, 16)], name=-), right=IdReference(location=[(2, 18) (2, 18)], name=b)))])"
        )
    }


    "\"typealias IntToInt = Int -> Int\nlet negate a : IntToInt = 0 - a\" should produce AST Module" {
        parse(
                "typealias IntToInt = Int -> Int\n" +
                        "let negate a : IntToInt = 0 - a",
                "Module(location=[(1, 0) (2, 30)], declarations=[TypeAliasDeclaration(location=[(1, 0) (1, 30)], name=ID(location=[(1, 10) (1, 17)], name=IntToInt), schema=TArrow(location=[(1, 21) (1, 30)], domain=TIdReference(location=[(1, 21) (1, 23)], name=Int), range=TIdReference(location=[(1, 28) (1, 30)], name=Int))), LetDeclaration(location=[(2, 0) (2, 30)], name=ID(location=[(2, 4) (2, 9)], name=negate), arguments=[ID(location=[(2, 11) (2, 11)], name=a)], schema=TIdReference(location=[(2, 15) (2, 22)], name=IntToInt), expression=BinaryOpExpression(location=[(2, 26) (2, 30)], left=ConstantInt(location=[(2, 26) (2, 26)], value=0), operator=ID(location=[(2, 28) (2, 28)], name=-), right=IdReference(location=[(2, 30) (2, 30)], name=a)))])"
        )
    }
})
