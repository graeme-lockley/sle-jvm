package za.co.no9.sle.transform.typelessPatternToTypedPattern

import za.co.no9.sle.ast.typelessPattern.BinaryOpExpression


enum class Associativity {
    Left, Right, None
}


data class Precedence(
        val level: Int,
        val associativity: Associativity)


val operators = mapOf(
        Pair("*", Precedence(7, Associativity.Left)),
        Pair("/", Precedence(7, Associativity.Left)),

        Pair("+", Precedence(6, Associativity.Left)),
        Pair("-", Precedence(6, Associativity.Left)),

        Pair("==", Precedence(4, Associativity.Left)),
        Pair("!=", Precedence(4, Associativity.Left)),
        Pair("<", Precedence(4, Associativity.None)),
        Pair("<=", Precedence(4, Associativity.None)),
        Pair(">", Precedence(4, Associativity.None)),
        Pair(">=", Precedence(4, Associativity.None)),

        Pair("&&", Precedence(3, Associativity.Right)),
        Pair("||", Precedence(2, Associativity.Right))
)


fun transformOperators(binary: BinaryOpExpression): BinaryOpExpression {
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
                operators[binary.operator.name]

        val rightOperatorPrecedence =
                operators[right.operator.name]

        if (operatorPrecedence == null) {
            return binary
        } else if (rightOperatorPrecedence == null) {
            return binary
        } else if (operatorPrecedence.level > rightOperatorPrecedence.level) {
            return transformOperators(rotate())
        } else if (operatorPrecedence.level == rightOperatorPrecedence.level && operatorPrecedence.associativity == Associativity.Left) {
            return transformOperators(rotate())
        } else {
            return BinaryOpExpression(binary.location, binary.left, binary.operator, transformOperators(right))
        }
    } else
        return binary
}
