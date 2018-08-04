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
            mutableSetOf<Error>()


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
                                val (type, typeConstraints) =
                                        dSchema.instantiate(varPump)

                                unify(type, e.type)
                                constraints += typeConstraints
                            }

                            LetDeclaration(d.location, generalise(e.type), ID(d.name.location, d.name.name), e)
                        }

                        is za.co.no9.sle.astToCoreAST.TypeAliasDeclaration ->
                            TypeAliasDeclaration(d.location, ID(d.name.location, d.name.name), d.schema)
                    }
                })
    }


    private fun infer(expression: za.co.no9.sle.astToCoreAST.Expression): Expression =
            when (expression) {
                is za.co.no9.sle.astToCoreAST.Unit ->
                    Unit(expression.location, typeUnit)

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

                        else -> {
                            val (type, typeConstraints) =
                                    schema.instantiate(varPump)

                            constraints += typeConstraints
                            IdReference(expression.location, type, expression.name)
                        }
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


fun infer2(varPump: VarPump, module: za.co.no9.sle.astToCoreAST.Module, env: Environment): Either<Errors, Module> {
    val context =
            InferContext2(varPump, env)

    val m =
            context.infer(module)

    return if (context.errors.isEmpty())
        value(m)
    else
        error(context.errors)
}


private class InferContext2(private val varPump: VarPump, internal var env: Environment) {
    var constraints =
            Constraints()

    val errors =
            mutableSetOf<Error>()


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

        val aliases =
                module.declarations
                        .filter { it is za.co.no9.sle.astToCoreAST.TypeAliasDeclaration }
                        .map { it as za.co.no9.sle.astToCoreAST.TypeAliasDeclaration }
                        .fold(emptyMap<String, Schema>()) { aliases, alias -> aliases + Pair(alias.name.name, alias.schema) }


        val declarations =
                module.declarations.fold(Pair(emptyList<za.co.no9.sle.inference.Declaration>(), env)) { ds, d ->
                    when (d) {
                        is za.co.no9.sle.astToCoreAST.LetDeclaration -> {
                            constraints =
                                    Constraints()

                            val e =
                                    infer(d.expression)

                            val dSchema =
                                    d.schema

                            if (dSchema == null) {
                                val unifiesResult =
                                        unifies(varPump, aliases, constraints)

                                val unifyErrors =
                                        unifiesResult.left()

                                val substitution =
                                        unifiesResult.right()

                                if (unifyErrors != null) {
                                    errors.addAll(unifyErrors)
                                }

                                val schema =
                                        if (substitution != null) {
                                            generalise(e.type, substitution)
                                        } else {
                                            generalise(e.type)
                                        }

                                val declaration =
                                        if (substitution != null) {
                                            LetDeclaration(d.location, schema, ID(d.name.location, d.name.name), e).apply(substitution)
                                        } else {
                                            LetDeclaration(d.location, schema, ID(d.name.location, d.name.name), e)
                                        }

                                Pair(ds.first + declaration, env.set(d.name.name, schema))
                            } else {
                                val (type, typeConstraints) =
                                        dSchema.instantiate(varPump)

                                unify(type, e.type)
                                constraints += typeConstraints

                                Pair(ds.first + LetDeclaration(d.location, dSchema, ID(d.name.location, d.name.name), e), ds.second)
                            }
                        }

                        is za.co.no9.sle.astToCoreAST.TypeAliasDeclaration ->
                            Pair(ds.first + TypeAliasDeclaration(d.location, ID(d.name.location, d.name.name), d.schema), ds.second)
                    }
                }

        return Module(
                module.location,
                declarations.first)
    }


    private fun infer(expression: za.co.no9.sle.astToCoreAST.Expression): Expression =
            when (expression) {
                is za.co.no9.sle.astToCoreAST.Unit ->
                    Unit(expression.location, typeUnit)

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

                        else -> {
                            val (type, typeConstraints) =
                                    schema.instantiate(varPump)

                            constraints += typeConstraints
                            IdReference(expression.location, type, expression.name)
                        }
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