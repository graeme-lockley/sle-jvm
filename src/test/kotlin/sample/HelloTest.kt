package sample

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import org.junit.Test
import kotlin.test.assertEquals

class HelloTest {
    @Test
    fun given_a_string_it_parses() {
        val lexer = ExprLexer(CharStreams.fromString("1+1\n"))
        val tokens = CommonTokenStream(lexer)

        val parser = ExprParser(tokens)
        val p : ParseTree = parser.prog()

        assertEquals("(prog (expr (expr 1) + (expr 1)) \\n)", p.toStringTree(parser))
    }
}
