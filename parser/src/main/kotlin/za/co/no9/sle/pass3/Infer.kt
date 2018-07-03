package za.co.no9.sle.pass3

import za.co.no9.sle.*
import za.co.no9.sle.pass2.Declaration


data class Constraint(val t1: Type, val t2: Type) {
    fun apply(substitution: Substitution): Constraint =
            Constraint(t1.apply(substitution), t2.apply(substitution))

    override fun toString(): String =
            "$t1 : $t2"
}


data class Constraints(private val state: List<Constraint> = emptyList()) {
    operator fun plus(constraint: Constraint): Constraints =
            Constraints(state + constraint)

    operator fun plus(constraints: Constraints): Constraints =
            Constraints(state + constraints.state)

    fun apply(substitution: Substitution): Constraints =
            Constraints(state.map { it.apply(substitution) })

    fun isNotEmpty(): Boolean =
            state.isNotEmpty()

    operator fun get(index: Int): Constraint =
            state[index]

    fun drop(count: Int): Constraints =
            Constraints(state.drop(count))

    override fun toString(): String =
            state.joinToString(", ") { it.toString() }
}


val noConstraints =
        Constraints()


fun infer(module: za.co.no9.sle.pass2.Module, env: Environment): Either<Errors, Pair<Module, Constraints>> {
    val context =
            InferContext(env)

    val m =
            context.infer(module)

    return if (context.errors.isEmpty())
        value(Pair(m, context.constraints))
    else
        error(context.errors)
}


fun infer(expression: za.co.no9.sle.pass2.Expression, env: Environment): Either<Errors, Pair<Expression, Constraints>> {
    val context =
            InferContext(env)

    val t =
            context.infer(expression)

    return when {
        context.errors.isEmpty() ->
            value(Pair(t, context.constraints))

        else ->
            error(context.errors)
    }
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
            Substitution(this.variable.zip(asP).toMap())

    return this.type.apply(substitution)
}


private class InferContext(internal var env: Environment) {
    private val varPump =
            VarPump()

    var constraints =
            Constraints()

    val errors =
            mutableListOf<Error>()


    fun infer(module: za.co.no9.sle.pass2.Module): Module {
        env = module.declarations.fold(env) { e: Environment, d: Declaration ->
            when (d) {
                is za.co.no9.sle.pass2.LetDeclaration -> {
                    val name =
                            d.name.name

                    if (env.containsKey(name)) {
                        errors.add(DuplicateLetDeclaration(d.location, name))
                        e
                    } else {
                        e.set(name, Schema(listOf(0), TVar(0)))
                    }
                }
            }
        }

        return Module(
                module.location,
                module.declarations.map { d ->
                    when (d) {
                        is za.co.no9.sle.pass2.LetDeclaration -> {
                            val e =
                                    infer(d.expression)

                            LetDeclaration(d.location, e.type, ID(d.name.location, d.name.name), e)
                        }
                    }
                })
    }

    fun infer(expression: za.co.no9.sle.pass2.Expression): Expression =
            when (expression) {
                is za.co.no9.sle.pass2.ConstantBool ->
                    ConstantBool(expression.location, typeBool, expression.value)

                is za.co.no9.sle.pass2.ConstantInt ->
                    ConstantInt(expression.location, typeInt, expression.value)

                is za.co.no9.sle.pass2.ConstantString ->
                    ConstantString(expression.location, typeString, expression.value)

                is za.co.no9.sle.pass2.IdReference -> {
                    val schema =
                            env[expression.name]

                    when (schema) {
                        null -> {
                            errors.add(UnboundVariable(expression.location, expression.name))

                            IdReference(expression.location, typeError, expression.name)
                        }

                        else ->
                            IdReference(expression.location, schema.instantiate(varPump), expression.name)
                    }
                }

                is za.co.no9.sle.pass2.IfExpression -> {
                    val t1 =
                            infer(expression.guardExpression)

                    val t2 =
                            infer(expression.thenExpression)

                    val t3 =
                            infer(expression.elseExpression)

                    unify(t1.type, typeBool)
                    unify(t2.type, t3.type)

                    IfExpression(expression.location, t2.type, t1, t2, t3)
                }

                is za.co.no9.sle.pass2.LambdaExpression -> {
                    val tv =
                            varPump.fresh()

                    val currentEnv = env

                    env = env.set(expression.argument.name, Schema(emptyList(), tv))

                    val t =
                            infer(expression.expression)

                    env = currentEnv

                    LambdaExpression(expression.location, TArr(tv, t.type), ID(expression.argument.location, expression.argument.name), t)
                }

                is za.co.no9.sle.pass2.CallExpression -> {
                    val t1 =
                            infer(expression.operator)

                    val t2 =
                            infer(expression.operand)

                    val tv =
                            varPump.fresh()

                    unify(t1.type, TArr(t2.type, tv))

                    CallExpression(expression.location, tv, t1, t2)
                }
            }

    private fun unify(t1: Type, t2: Type) {
        constraints += Constraint(t1, t2)
    }
}