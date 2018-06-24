package za.co.no9.sle.ast.pass3

import za.co.no9.sle.*
import za.co.no9.sle.ast.pass2.*


fun infer(expression: Expression, env: Environment): Either<Error, Type> =
        MapState(env).infer(expression)


private class VarPump {
    private var counter =
            0

    fun fresh(): TVar {
        val result =
                counter

        counter += 1

        return TVar(result)
    }
}


private fun Schema.instantiate(varPump: VarPump): Type {
    val asP =
            this.variable.map { varPump.fresh() }

    val substitution =
            this.variable.zip(asP).toMap()

    return this.type.apply(substitution)
}


class MapState(var env: Environment) {
    private val varPump =
            VarPump()


    fun infer(expression: Expression): Either<Error, Type> =
            when (expression) {
                is ConstantBool ->
                    value(typeBool)

                is ConstantInt ->
                    value(typeInt)

                is ConstantString ->
                    value(typeString)

                is IdReference -> {
                    val schema =
                            env.lookup(expression.name)

                    when (schema) {
                        null ->
                            error(UnboundVariable(expression.location, expression.name))

                        else ->
                            value(schema.instantiate(varPump))
                    }
                }

                is IfExpression -> TODO()
                is LambdaExpression -> TODO()
                is CallExpression -> TODO()
            }
}