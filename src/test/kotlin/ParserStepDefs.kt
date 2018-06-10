import cucumber.api.java8.En
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import za.co.no9.sle.ParserLexer
import za.co.no9.sle.ParserParser
import kotlin.test.assertEquals


private object State {
    var inputText: String? = null

    var parser: ParserParser? = null
    var parseTree: ParseTree? = null
}


class ParserStepDefs : En {
    init {
        Given("the sle text {string}") { string: String ->
            State.inputText = string
        }

        When("I parse the supplied input as a module") {
            if (State.inputText == null) {
                throw IllegalArgumentException("Unable to parse as no input has been supplied")
            } else {
                val lexer = ParserLexer(CharStreams.fromString(State.inputText))
                val tokens = CommonTokenStream(lexer)

                val parser = ParserParser(tokens)
                State.parseTree = parser.module()

                State.parser = parser
            }
        }

        Then("the parse tree is {string}") { string: String ->
            val parseTree = State.parseTree

            if (parseTree == null) {
                throw IllegalArgumentException("Unable to validate parse tree as it is not set")
            } else {
                val parseTreeValue = parseTree.toStringTree(State.parser)

                assertEquals(string, parseTreeValue)
            }
        }
    }
}