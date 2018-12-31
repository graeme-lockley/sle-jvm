package za.co.no9.sle.transform.typelessPatternToTypedPattern

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.ast.typelessPattern.*
import za.co.no9.sle.homeLocation
import za.co.no9.sle.typing.*


class TransformOperatorsTests : StringSpec({
    "c >= 'a' && c <= 'z'" {
        val input =
                BinaryOpExpression(homeLocation,
                        IdReference(homeLocation, QualifiedID(homeLocation, "null", "c")),
                        ID(homeLocation, ">="),
                        BinaryOpExpression(homeLocation,
                                ConstantChar(homeLocation, 'a'),
                                ID(homeLocation, "&&"),
                                BinaryOpExpression(homeLocation,
                                        IdReference(homeLocation, QualifiedID(homeLocation, "null", "c")),
                                        ID(homeLocation, "<="),
                                        ConstantChar(homeLocation, 'z')
                                )
                        )
                )

        val environment =
                initialEnvironment
                        .newValue("&&",
                                OperatorBinding(
                                        Scheme(listOf(), TArr(typeBool, TArr(typeBool, typeBool))),
                                        3,
                                        Right
                                ))
                        .newValue(">=",
                                OperatorBinding(
                                        Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool))),
                                        4,
                                        None
                                ))
                        .newValue("<=",
                                OperatorBinding(
                                        Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool))),
                                        4,
                                        None
                                ))

        val output =
                transformOperators(environment, input)

        output.shouldBe(
                BinaryOpExpression(homeLocation,
                        BinaryOpExpression(homeLocation,
                                IdReference(homeLocation, QualifiedID(homeLocation, "null", "c")),
                                ID(homeLocation, ">="),
                                ConstantChar(homeLocation, 'a')),
                        ID(homeLocation, "&&"),
                        BinaryOpExpression(homeLocation,
                                IdReference(homeLocation, QualifiedID(homeLocation, "null", "c")),
                                ID(homeLocation, "<="),
                                ConstantChar(homeLocation, 'z')
                        )
                )
        )
    }
})
