package za.co.no9.sle.pass1

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.Either
import za.co.no9.sle.parser.Result
import za.co.no9.sle.parser.parseText
import za.co.no9.sle.right


class DeclarationTests : StringSpec({
    fun parse(input: String, output: String) {
        val parseResult =
                parseText(input)

        parseResult.shouldBeTypeOf<Either.Value<Result>>()
        toModule(parseResult.right()!!.node).toString().shouldBe(output)
    }


    "\"let add x y = x + y\" should product AST LetDeclaration" {
        parse(
                "let add x y = x + y",
                "Module(location=[(1, 0) (1, 18)], declarations=[LetDeclaration(location=[(1, 0) (1, 18)], name=ID(location=[(1, 4) (1, 6)], name=add), arguments=[ID(location=[(1, 8) (1, 8)], name=x), ID(location=[(1, 10) (1, 10)], name=y)], expression=BinaryOpExpression(location=[(1, 14) (1, 18)], left=IdReference(location=[(1, 14) (1, 14)], name=x), operator=ID(location=[(1, 16) (1, 16)], name=+), right=IdReference(location=[(1, 18) (1, 18)], name=y)))])"
        )
    }


    "\"let add x y = x + y\nlet sub a b = a - b\" should product AST Module" {
        parse(
                "let add x y = x + y\n" +
                        "let sub a b = a - b",
                "Module(location=[(1, 0) (2, 18)], declarations=[LetDeclaration(location=[(1, 0) (1, 18)], name=ID(location=[(1, 4) (1, 6)], name=add), arguments=[ID(location=[(1, 8) (1, 8)], name=x), ID(location=[(1, 10) (1, 10)], name=y)], expression=BinaryOpExpression(location=[(1, 14) (1, 18)], left=IdReference(location=[(1, 14) (1, 14)], name=x), operator=ID(location=[(1, 16) (1, 16)], name=+), right=IdReference(location=[(1, 18) (1, 18)], name=y))), LetDeclaration(location=[(2, 0) (2, 18)], name=ID(location=[(2, 4) (2, 6)], name=sub), arguments=[ID(location=[(2, 8) (2, 8)], name=a), ID(location=[(2, 10) (2, 10)], name=b)], expression=BinaryOpExpression(location=[(2, 14) (2, 18)], left=IdReference(location=[(2, 14) (2, 14)], name=a), operator=ID(location=[(2, 16) (2, 16)], name=-), right=IdReference(location=[(2, 18) (2, 18)], name=b)))])"
        )
    }
})
