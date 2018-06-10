import cucumber.api.java8.En
import za.co.no9.sle.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


private object State {
    var inputText: String? = null

    var parseResult: ParseResult = value("")
}


class ParserStepDefs : En {
    init {
        Given("the sle text {string}") { string: String ->
            State.inputText = string
        }

        When("I parse the supplied input as a module") {
            val inputText = State.inputText

            if (inputText == null) {
                throw IllegalArgumentException("Unable to parse as no input has been supplied")
            } else {
                State.parseResult = parseText(inputText)
            }
        }

        Then("the parse tree is {string}") { string: String ->
            val parseTree = State.parseResult.right()

            assertEquals(string, parseTree ?: "")
        }

        Then("I expect a syntax error to be reported") {
            assertNotNull(State.parseResult.left())
        }
    }
}