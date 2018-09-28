package za.co.no9.sle.transform.typelessCoreToTypedCore

import za.co.no9.sle.*
import za.co.no9.sle.ast.typedCore.*
import za.co.no9.sle.ast.typedCore.Unit
import za.co.no9.sle.ast.typelessCore.Declaration
import za.co.no9.sle.ast.typelessCore.TypeDeclaration
import za.co.no9.sle.typing.*


fun infer(varPump: VarPump, module: za.co.no9.sle.ast.typelessCore.Module, env: Environment): Either<Errors, Pair<Module, Constraints>> {
    val context =
            InferContext(varPump, env)

    val m =
            context.infer(module)

    return if (context.errors.isEmpty())
        value(Pair(m, context.constraints))
    else
        error(context.errors)
}


fun infer2(varPump: VarPump, module: za.co.no9.sle.ast.typelessCore.Module, env: Environment): Either<Errors, Module> {
    val context =
            InferContext(varPump, env)

    val m =
            context.infer(module)

    return if (context.errors.isEmpty())
        value(m)
    else
        error(context.errors)
}


private class InferContext(private val varPump: VarPump, internal var env: Environment) {
    var constraints =
            Constraints()

    val errors =
            mutableSetOf<Error>()


    fun infer(module: za.co.no9.sle.ast.typelessCore.Module): Module {
        reportDuplicateLetDeclarationNames(module)

        env = module.declarations.fold(env) { e: Environment, d: Declaration ->
            when (d) {
                is za.co.no9.sle.ast.typelessCore.LetDeclaration -> {
                    val name =
                            d.name.name

                    val scheme =
                            d.scheme

                    if (scheme == null) {
                        e
                    } else {
                        e.newValue(name, scheme)
                    }
                }

                is za.co.no9.sle.ast.typelessCore.TypeAliasDeclaration ->
                    e

                is za.co.no9.sle.ast.typelessCore.TypeDeclaration ->
                    d.constructors.fold(e) { fds, constructor ->
                        if (fds.containsValue(constructor.name.name)) {
                            errors.add(DuplicateConstructorDeclaration(constructor.location, constructor.name.name))
                            fds
                        } else {
                            val result =
                                    fds.newValue(constructor.name.name, Scheme(d.scheme.parameters, constructor.arguments.foldRight(d.scheme.type) { a, b -> TArr(a, b) }))

                            result
                        }
                    }
            }
        }

        val aliases =
                module.declarations
                        .filter { it is za.co.no9.sle.ast.typelessCore.TypeAliasDeclaration }
                        .map { it as za.co.no9.sle.ast.typelessCore.TypeAliasDeclaration }
                        .fold(emptyMap<String, Scheme>()) { aliases, alias -> aliases + Pair(alias.name.name, alias.scheme) }


        val declarations =
                module.declarations.fold(emptyList<za.co.no9.sle.ast.typedCore.Declaration>()) { ds, d ->
                    when (d) {
                        is za.co.no9.sle.ast.typelessCore.LetDeclaration -> {
                            val e =
                                    infer(d.expression)

                            val dScheme =
                                    d.scheme

                            if (dScheme == null) {
                                val unifiesResult =
                                        unifies(varPump, aliases, constraints)

                                val unifyErrors =
                                        unifiesResult.left()

                                val substitution =
                                        unifiesResult.right()

                                if (unifyErrors != null) {
                                    errors.addAll(unifyErrors)
                                }

                                val scheme =
                                        if (substitution != null) {
                                            generalise(e.type, substitution)
                                        } else {
                                            generalise(e.type)
                                        }

                                val declaration =
                                        if (substitution != null) {
                                            LetDeclaration(d.location, scheme, ID(d.name.location, d.name.name), e).apply(substitution)
                                        } else {
                                            LetDeclaration(d.location, scheme, ID(d.name.location, d.name.name), e)
                                        }


                                env = env.newValue(d.name.name, scheme)

                                ds + declaration
                            } else {
                                val type =
                                        dScheme.instantiate(varPump)

                                unify(type, e.type)

                                ds + LetDeclaration(d.location, dScheme, ID(d.name.location, d.name.name), e)
                            }
                        }

                        is za.co.no9.sle.ast.typelessCore.TypeAliasDeclaration ->
                            ds + TypeAliasDeclaration(d.location, ID(d.name.location, d.name.name), d.scheme)

                        is za.co.no9.sle.ast.typelessCore.TypeDeclaration ->
                            ds + za.co.no9.sle.ast.typedCore.TypeDeclaration(
                                    d.location,
                                    ID(d.name.location, d.name.name),
                                    d.scheme,
                                    d.constructors.map { Constructor(it.location, ID(it.name.location, it.name.name), it.arguments) }
                            )
                    }
                }

        return Module(
                module.location,
                declarations)
    }


    private fun reportDuplicateLetDeclarationNames(module: za.co.no9.sle.ast.typelessCore.Module) {
        module.declarations.fold(emptySet()) { e: Set<String>, d: Declaration ->
            when (d) {
                is za.co.no9.sle.ast.typelessCore.LetDeclaration -> {
                    val name =
                            d.name.name

                    if (e.contains(name)) {
                        errors.add(DuplicateLetDeclaration(d.location, name))
                        e
                    } else {
                        e + name
                    }
                }

                is za.co.no9.sle.ast.typelessCore.TypeAliasDeclaration ->
                    e

                is TypeDeclaration ->
                    e
            }
        }
    }


    private fun infer(expression: za.co.no9.sle.ast.typelessCore.Expression): Expression =
            when (expression) {
                is za.co.no9.sle.ast.typelessCore.Unit ->
                    Unit(expression.location, typeUnit)

                is za.co.no9.sle.ast.typelessCore.ConstantBool ->
                    ConstantBool(expression.location, typeBool, expression.value)

                is za.co.no9.sle.ast.typelessCore.ConstantInt ->
                    ConstantInt(expression.location, typeInt, expression.value)

                is za.co.no9.sle.ast.typelessCore.ConstantString ->
                    ConstantString(expression.location, typeString, expression.value)

                is za.co.no9.sle.ast.typelessCore.IdReference -> {
                    val scheme =
                            env.value(expression.name)

                    when (scheme) {
                        null -> {
                            errors.add(UnboundVariable(expression.location, expression.name))

                            IdReference(expression.location, typeError, expression.name)
                        }

                        else -> {
                            val type =
                                    scheme.instantiate(varPump)

                            IdReference(expression.location, type, expression.name)
                        }
                    }
                }

                is za.co.no9.sle.ast.typelessCore.ConstructorReference -> {
                    val scheme =
                            env.value(expression.name)

                    when (scheme) {
                        null -> {
                            errors.add(UnboundVariable(expression.location, expression.name))

                            IdReference(expression.location, typeError, expression.name)
                        }

                        else -> {
                            val type =
                                    scheme.instantiate(varPump)

                            IdReference(expression.location, type, expression.name)
                        }
                    }
                }

                is za.co.no9.sle.ast.typelessCore.IfExpression -> {
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

                is za.co.no9.sle.ast.typelessCore.LambdaExpression -> {
                    val tv =
                            varPump.fresh()

                    val currentEnv = env

                    env = env.newValue(expression.argument.name, Scheme(emptyList(), tv))

                    val t =
                            infer(expression.expression)

                    env = currentEnv

                    LambdaExpression(expression.location, TArr(tv, t.type), ID(expression.argument.location, expression.argument.name), t)
                }

                is za.co.no9.sle.ast.typelessCore.CallExpression -> {
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