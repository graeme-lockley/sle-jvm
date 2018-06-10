import cucumber.api.java8.En
import za.co.no9.sle.ParseState
import za.co.no9.sle.parseText
import kotlin.test.assertEquals


private object State {
    var inputText: String? = null

    var parseState: ParseState? = null
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
                State.parseState = parseText(inputText)
            }
        }

        Then("the parse tree is {string}") { string: String ->
            val parseState = State.parseState

            if (parseState == null) {
                throw IllegalArgumentException("Unable to validate parse tree as it is not set")
            } else {
                assertEquals(string, parseState.stringTree)
            }
        }
    }
}