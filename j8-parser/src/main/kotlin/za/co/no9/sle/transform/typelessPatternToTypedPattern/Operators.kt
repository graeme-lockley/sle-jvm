package za.co.no9.sle.transform.typelessPatternToTypedPattern

import za.co.no9.sle.ast.typelessPattern.BinaryOpExpression
import za.co.no9.sle.typing.Associativity
import za.co.no9.sle.typing.Environment
import za.co.no9.sle.typing.OperatorBinding


fun transformOperators(environment: Environment, binary: BinaryOpExpression): BinaryOpExpression {
    val right =
            binary.right

    if (right is BinaryOpExpression) {
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

        if (operatorPrecedence == null) {
            return binary
        } else if (rightOperatorPrecedence == null) {
            return binary
        } else if (operatorPrecedence.precedence > rightOperatorPrecedence.precedence) {
            return transformOperators(environment, rotate())
        } else if (operatorPrecedence.precedence == rightOperatorPrecedence.precedence && operatorPrecedence.associativity == Associativity.Left) {
            return transformOperators(environment, rotate())
        } else {
            return BinaryOpExpression(binary.location, binary.left, binary.operator, transformOperators(environment, right))
        }
    } else
        return binary
}


private fun Environment.operator(name: String): OperatorBinding? {
    val value =
            this.value(name)

    return if (value is OperatorBinding)
        value
    else
        null
}