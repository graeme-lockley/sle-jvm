import cucumber.api.java8.En
import za.co.no9.sle.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class ParserStepDefs : En {
    init {
        var inputText: String? = null

        var parseResult: ParseResult? = null


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


        When("I parse the input as a factor") {
            val text = inputText

            if (text == null) {
                throw IllegalArgumentException("Unable to parse as no input has been supplied")
            } else {
                parseResult = parseTextAsFactor(text)
            }
        }


        Then("the parse tree is {string}") { string: String ->
            val parseTree = parseResult?.right()

            assertEquals(string, parseTree?.stringTree)
        }


        Then("I expect a syntax error to be reported") {
            assertNotNull(parseResult?.left())
        }


        Then("I expect the AST to be {string}") { string: String ->
        }


        Then("no type constraints are inferred") {
            val parseTree = parseResult?.right()

            assertEquals(0, parseTree?.constraints?.size)
        }


        Then("the tree's schema is {string}") { string: String ->
        }
    }
}