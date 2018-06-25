package za.co.no9.sle.ast.pass3

import za.co.no9.sle.*
import za.co.no9.sle.ast.pass2.*


typealias Constraints =
        List<Pair<Type, Type>>


fun infer(expression: Expression, env: Environment): Either<Error, Type> =
        MapState(env).infer(expression)


fun constraints(expression: Expression, env: Environment): Constraints {
    val state =
            MapState(env)

    state.infer(expression)

    return state.constraints
}


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


class MapState(val env: Environment) {
    private val varPump =
            VarPump()

    val constraints =
            mutableListOf<Pair<Type, Type>>()


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

                is IfExpression -> {
                    val t1 =
                            infer(expression.guardExpression)

                    val t2 =
                            infer(expression.thenExpression)

                    val t3 =
                            infer(expression.elseExpression)

                    unify(t1, value(typeBool))
                    unify(t2, t3)

                    t2
                }

                is LambdaExpression -> {
                    val tv =
                            varPump.fresh()

                    env.openScope()
                    env.bindInScope(expression.argument.name, Schema(emptyList(), tv))
                    val t =
                            infer(expression.expression)
                    env.closeScope()

                    t.map { TArr(tv, it) }
                }

                is CallExpression -> TODO()
            }

    private fun unify(et1: Either<Error, Type>, et2: Either<Error, Type>) {
        val t1 =
                et1.right()

        val t2 =
                et2.right()

        if (t1 != null && t2 != null) {
            constraints.add(Pair(t1, t2))
        }
    }
}