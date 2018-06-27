package za.co.no9.sle.ast.pass3

import za.co.no9.sle.*
import za.co.no9.sle.ast.pass2.*


typealias Constraints =
        List<Pair<Type, Type>>


fun infer(expression: Expression, env: Environment): Either<Errors, Type> {
    val context =
            InferContext(env)

    val t =
            context.infer(expression)

    return if (context.errors.isEmpty())
        value(t)
    else
        (error(context.errors))
}


fun constraints(expression: Expression, env: Environment): Constraints {
    val state =
            InferContext(env)

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


private class InferContext(private var env: Environment) {
    private val varPump =
            VarPump()

    val constraints =
            mutableListOf<Pair<Type, Type>>()

    val errors =
            mutableListOf<Error>()


    fun infer(expression: Expression): Type =
            when (expression) {
                is ConstantBool ->
                    typeBool

                is ConstantInt ->
                    typeInt

                is ConstantString ->
                    typeString

                is IdReference -> {
                    val schema =
                            env.lookup(expression.name)

                    when (schema) {
                        null -> {
                            errors.add(UnboundVariable(expression.location, expression.name))

                            typeError
                        }

                        else ->
                            schema.instantiate(varPump)
                    }
                }

                is IfExpression -> {
                    val t1 =
                            infer(expression.guardExpression)

                    val t2 =
                            infer(expression.thenExpression)

                    val t3 =
                            infer(expression.elseExpression)

                    unify(t1, typeBool)
                    unify(t2, t3)

                    t2
                }

                is LambdaExpression -> {
                    val tv =
                            varPump.fresh()

                    val currentEnv = env

                    env = env.bindInScope(expression.argument.name, Schema(emptyList(), tv))

                    val t =
                            infer(expression.expression)

                    env = currentEnv

                    TArr(tv, t)
                }

                is CallExpression -> {
                    val t1 =
                            infer(expression.operator)

                    val t2 =
                            infer(expression.operands)

                    val tv =
                            varPump.fresh()

                    unify(t1, TArr(t2, tv))

                    tv
                }
            }

    private fun unify(t1: Type, t2: Type) {
        constraints.add(Pair(t1, t2))
    }
}