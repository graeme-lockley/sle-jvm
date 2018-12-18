package za.co.no9.sle.transform.typelessPatternToTypedPattern

import za.co.no9.sle.ast.typelessPattern.BinaryOpExpression
import za.co.no9.sle.typing.Environment
import za.co.no9.sle.typing.Left
import za.co.no9.sle.typing.OperatorBinding


fun transformOperators(environment: Environment, binary: BinaryOpExpression): BinaryOpExpression {
    val right =
            binary.right

    return if (right is BinaryOpExpression) {
        fun rotate() =
                BinaryOpExpression(
                        right.location,
                        BinaryOpExpression(binary.location, binary.left, binary.operator, right.left),
                        right.operator,
                        right.right)

        val operatorPrecedence =
                environment.operator(binary.operator.name)

        val rightOperatorPrecedence =
                environment.operator(right.operator.name)

        when {
            operatorPrecedence == null ->
                binary

            rightOperatorPrecedence == null ->
                binary

            operatorPrecedence.precedence > rightOperatorPrecedence.precedence ->
                transformOperators(environment, rotate())

            operatorPrecedence.precedence == rightOperatorPrecedence.precedence && operatorPrecedence.associativity == Left ->
                transformOperators(environment, rotate())

            else ->
                BinaryOpExpression(binary.location, binary.left, binary.operator, transformOperators(environment, right))
        }
    } else
        binary
}


private fun Environment.operator(name: String): OperatorBinding? {
    val value =
            this.value(name)

    return if (value is OperatorBinding)
        value
    else
        null
}