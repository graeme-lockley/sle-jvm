import cucumber.api.java8.En
import za.co.no9.sle.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class ParserStepDefs : En {
    init {
        var inputText: String? = null

        var parseResult: ParseResult = value("")

        Given("the sle text {string}") { string: String ->
            inputText = string
        }

        When("I parse the supplied input as a module") {
            val text = inputText

            if (text == null) {
                throw IllegalArgumentException("Unable to parse as no input has been supplied")
            } else {
                parseResult = parseText(text)
            }
        }

        Then("the parse tree is {string}") { string: String ->
            val parseTree = parseResult.right()

            assertEquals(string, parseTree ?: "")
        }

        Then("I expect a syntax error to be reported") {
            assertNotNull(parseResult.left())
        }
    }
}