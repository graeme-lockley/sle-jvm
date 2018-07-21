package za.co.no9.sle.inference

import za.co.no9.sle.*
import za.co.no9.sle.astToCoreAST.Declaration
import za.co.no9.sle.typing.*


fun infer(varPump: VarPump, module: za.co.no9.sle.astToCoreAST.Module, env: Environment): Either<Errors, Pair<Module, Constraints>> {
    val context =
            InferContext(varPump, env)

    val m =
            context.infer(module)

    return if (context.errors.isEmpty())
        value(Pair(m, context.constraints))
    else
        error(context.errors)
}


private class InferContext(private val varPump: VarPump, internal var env: Environment) {
    var constraints =
            Constraints()

    val errors =
            mutableListOf<Error>()


    fun infer(module: za.co.no9.sle.astToCoreAST.Module): Module {
        module.declarations.fold(emptySet()) { e: Set<String>, d: Declaration ->
            when (d) {
                is za.co.no9.sle.astToCoreAST.LetDeclaration -> {
                    val name =
                            d.name.name

                    if (e.contains(name)) {
                        errors.add(DuplicateLetDeclaration(d.location, name))
                        e
                    } else {
                        e + name
                    }
                }

                is za.co.no9.sle.astToCoreAST.TypeAliasDeclaration ->
                    e
            }
        }

        env = module.declarations.fold(env) { e: Environment, d: Declaration ->
            when (d) {
                is za.co.no9.sle.astToCoreAST.LetDeclaration -> {
                    val name =
                            d.name.name

                    val schema =
                            d.schema

                    if (schema == null) {
                        e
                    } else {
                        e.set(name, schema)
                    }
                }

                is za.co.no9.sle.astToCoreAST.TypeAliasDeclaration ->
                    e
            }
        }

        return Module(
                module.location,
                module.declarations.map { d ->
                    when (d) {
                        is za.co.no9.sle.astToCoreAST.LetDeclaration -> {
                            val e =
                                    infer(d.expression)

                            val dSchema =
                                    d.schema

                            if (dSchema != null) {
                                unify(dSchema.instantiate(varPump), e.type)
                            }

                            LetDeclaration(d.location, e.type, ID(d.name.location, d.name.name), e)
                        }

                        is za.co.no9.sle.astToCoreAST.TypeAliasDeclaration ->
                            TypeAliasDeclaration(d.location, ID(d.name.location, d.name.name), d.schema)
                    }
                })
    }

    fun infer(expression: za.co.no9.sle.astToCoreAST.Expression): Expression =
            when (expression) {
                is za.co.no9.sle.astToCoreAST.ConstantBool ->
                    ConstantBool(expression.location, typeBool, expression.value)

                is za.co.no9.sle.astToCoreAST.ConstantInt ->
                    ConstantInt(expression.location, typeInt, expression.value)

                is za.co.no9.sle.astToCoreAST.ConstantString ->
                    ConstantString(expression.location, typeString, expression.value)

                is za.co.no9.sle.astToCoreAST.IdReference -> {
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

                is za.co.no9.sle.astToCoreAST.IfExpression -> {
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

                is za.co.no9.sle.astToCoreAST.LambdaExpression -> {
                    val tv =
                            varPump.fresh()

                    val currentEnv = env

                    env = env.set(expression.argument.name, Schema(emptyList(), tv))

                    val t =
                            infer(expression.expression)

                    env = currentEnv

                    LambdaExpression(expression.location, TArr(tv, t.type), ID(expression.argument.location, expression.argument.name), t)
                }

                is za.co.no9.sle.astToCoreAST.CallExpression -> {
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