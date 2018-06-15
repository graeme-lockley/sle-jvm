package za.co.no9.sle

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.ast.True

class ParserASTTests : StringSpec({
    "\"True\" should produce corresponding AST" {
        val result =
                parseTextAsFactor("True")

        val expression =
                result.right()?.parserToAST()?.popExpression()

        expression!!.shouldBeTypeOf<True>()
    }
})