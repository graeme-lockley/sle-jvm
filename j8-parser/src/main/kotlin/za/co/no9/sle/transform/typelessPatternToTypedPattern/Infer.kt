package za.co.no9.sle.transform.typelessPatternToTypedPattern

import za.co.no9.sle.*
import za.co.no9.sle.ast.typedPattern.*
import za.co.no9.sle.ast.typedPattern.Unit
import za.co.no9.sle.ast.typelessPattern.Declaration
import za.co.no9.sle.ast.typelessPattern.TypeDeclaration
import za.co.no9.sle.typing.*


fun infer(varPump: VarPump, module: za.co.no9.sle.ast.typelessPattern.Module, env: Environment): Either<Errors, Pair<Module, Constraints>> {
    val context =
            InferContext(varPump, env)

    val m =
            context.infer(module)

    return if (context.errors.isEmpty())
        value(Pair(m, context.constraints))
    else
        error(context.errors)
}


fun infer2(varPump: VarPump, module: za.co.no9.sle.ast.typelessPattern.Module, env: Environment): Either<Errors, Module> {
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


    fun infer(module: za.co.no9.sle.ast.typelessPattern.Module): Module {
        reportDuplicateLetDeclarationNames(module)

        env = module.declarations.fold(env) { e: Environment, d: Declaration ->
            when (d) {
                is za.co.no9.sle.ast.typelessPattern.LetDeclaration -> {
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

                is za.co.no9.sle.ast.typelessPattern.TypeAliasDeclaration -> {
                    if (e.containsType(d.name.name)) {
                        errors.add(DuplicateTypeAliasDeclaration(d.location, d.name.name))
                        e
                    } else {
                        e.newType(d.name.name, d.scheme)
                    }
                }

                is za.co.no9.sle.ast.typelessPattern.TypeDeclaration -> {
                    val newEnv =
                            d.constructors.fold(e) { fds, constructor ->
                                if (fds.containsValue(constructor.name.name)) {
                                    errors.add(DuplicateConstructorDeclaration(constructor.location, constructor.name.name))
                                    fds
                                } else {
                                    fds.newValue(constructor.name.name, Scheme(d.scheme.parameters, constructor.arguments.foldRight(d.scheme.type) { a, b -> TArr(a, b) }))
                                }
                            }

                    if (newEnv.containsType(d.name.name)) {
                        errors.add(DuplicateTypeDeclaration(d.location, d.name.name))
                        newEnv
                    } else {
                        newEnv.newType(d.name.name, d.scheme)
                    }
                }
            }
        }

        val aliases =
                module.declarations
                        .filter { it is za.co.no9.sle.ast.typelessPattern.TypeAliasDeclaration }
                        .map { it as za.co.no9.sle.ast.typelessPattern.TypeAliasDeclaration }
                        .fold(emptyMap<String, Scheme>()) { aliases, alias -> aliases + Pair(alias.name.name, alias.scheme) }


        for (declaration in module.declarations) {
            when (declaration) {
                is za.co.no9.sle.ast.typelessPattern.LetDeclaration -> {
                    val scheme =
                            declaration.scheme

                    if (scheme != null) {
                        validateScheme(scheme)
                    }
                }

                is za.co.no9.sle.ast.typelessPattern.TypeAliasDeclaration -> {
                    validateScheme(declaration.scheme)
                }

                is za.co.no9.sle.ast.typelessPattern.TypeDeclaration -> {
                    validateScheme(declaration.scheme)
                    for (constructor in declaration.constructors) {
                        for (type in constructor.arguments) {
                            validateType(type)
                        }
                    }
                }
            }
        }


        val declarations =
                module.declarations.fold(emptyList<za.co.no9.sle.ast.typedPattern.Declaration>()) { ds, d ->
                    when (d) {
                        is za.co.no9.sle.ast.typelessPattern.LetDeclaration -> {
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

                        is za.co.no9.sle.ast.typelessPattern.TypeAliasDeclaration ->
                            ds + TypeAliasDeclaration(d.location, ID(d.name.location, d.name.name), d.scheme)

                        is za.co.no9.sle.ast.typelessPattern.TypeDeclaration ->
                            ds + za.co.no9.sle.ast.typedPattern.TypeDeclaration(
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


    private fun reportDuplicateLetDeclarationNames(module: za.co.no9.sle.ast.typelessPattern.Module) {
        module.declarations.fold(emptySet()) { e: Set<String>, d: Declaration ->
            when (d) {
                is za.co.no9.sle.ast.typelessPattern.LetDeclaration -> {
                    val name =
                            d.name.name

                    if (e.contains(name)) {
                        errors.add(DuplicateLetDeclaration(d.location, name))
                        e
                    } else {
                        e + name
                    }
                }

                is za.co.no9.sle.ast.typelessPattern.TypeAliasDeclaration ->
                    e

                is TypeDeclaration ->
                    e
            }
        }
    }


    private fun validateScheme(scheme: Scheme) {
        validateType(scheme.type)
    }


    private fun validateType(type: Type) {
        when (type) {
            is TCon -> {
                val scheme =
                        env.typeBindings[type.name]

                if (scheme == null) {
                    val emptyLocation =
                            Location(Position(0, 0))

                    // TODO Pass in the type's actual position rather than creating an emptyLocation
                    errors.add(UnknownTypeReference(emptyLocation, type.name))
                } else if (type.arguments.size != scheme.parameters.size) {
                    val emptyLocation =
                            Location(Position(0, 0))

                    // TODO Pass in the type's actual position rather than creating an emptyLocation
                    errors.add(IncorrectNumberOfSchemeArguments(emptyLocation, type.name, scheme.parameters.size, type.arguments.size))
                }
            }

            is TArr -> {
                validateType(type.domain)
                validateType(type.range)
            }

            else -> {
            }
        }
    }


    private fun infer(expression: za.co.no9.sle.ast.typelessPattern.Expression): Expression =
            when (expression) {
                is za.co.no9.sle.ast.typelessPattern.Unit ->
                    Unit(expression.location, typeUnit)

                is za.co.no9.sle.ast.typelessPattern.ConstantBool ->
                    ConstantBool(expression.location, typeBool, expression.value)

                is za.co.no9.sle.ast.typelessPattern.ConstantInt ->
                    ConstantInt(expression.location, typeInt, expression.value)

                is za.co.no9.sle.ast.typelessPattern.ConstantString ->
                    ConstantString(expression.location, typeString, expression.value)

                is za.co.no9.sle.ast.typelessPattern.IdReference -> {
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

                is za.co.no9.sle.ast.typelessPattern.ConstructorReference -> {
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

                is za.co.no9.sle.ast.typelessPattern.IfExpression -> {
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

                is za.co.no9.sle.ast.typelessPattern.LambdaExpression -> {
                    val tv =
                            varPump.fresh()

                    val currentEnv = env

                    env = env.newValue(expression.argument.name, Scheme(emptyList(), tv))

                    val t =
                            infer(expression.expression)

                    env = currentEnv

                    LambdaExpression(expression.location, TArr(tv, t.type), ID(expression.argument.location, expression.argument.name), t)
                }

                is za.co.no9.sle.ast.typelessPattern.CallExpression -> {
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