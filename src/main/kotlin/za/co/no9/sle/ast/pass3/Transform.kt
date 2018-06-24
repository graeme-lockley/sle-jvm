package za.co.no9.sle.ast.pass3

import za.co.no9.sle.Type
import za.co.no9.sle.ast.pass2.*
import za.co.no9.sle.typeBool
import za.co.no9.sle.typeInt


fun infer(expression: Expression): Type =
        MapState().infer(expression)


class MapState {
    fun infer(expression: Expression): Type =
            when (expression) {
                is ConstantBool ->
                    typeBool

                is ConstantInt ->
                    typeInt

                is ConstantString -> TODO()
                is IdReference -> TODO()
                is IfExpression -> TODO()
                is LambdaExpression -> TODO()
                is CallExpression -> TODO()
            }
}