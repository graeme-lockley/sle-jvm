package za.co.no9.sle.inference

import za.co.no9.sle.Either
import za.co.no9.sle.Errors
import za.co.no9.sle.andThen
import za.co.no9.sle.ast.typedCore.Module
import za.co.no9.sle.ast.typedCore.TypeAliasDeclaration
import za.co.no9.sle.transform.typelessToTypelessCore.parse
import za.co.no9.sle.map
import za.co.no9.sle.typing.Environment
import za.co.no9.sle.typing.Substitution
import za.co.no9.sle.typing.VarPump


data class InferenceDetail(
        val constraints: Constraints,
        val substitution: Substitution,
        val unresolvedModule: Module,
        val resolvedModule: Module)


fun parseWithDetail(text: String, environment: Environment): Either<Errors, InferenceDetail> {
    val varPump =
            VarPump()

    fun aliases(module: Module): Aliases =
            module.declarations
                    .filter { it is TypeAliasDeclaration }
                    .map { it as TypeAliasDeclaration }
                    .fold(emptyMap()) { aliases, alias -> aliases + Pair(alias.name.name, alias.schema) }

    return parse(text)
            .andThen { infer(varPump, it, environment) }
            .andThen { (unresolvedModule, constraints) ->
                unifies(varPump, aliases(unresolvedModule), constraints).map { Triple(unresolvedModule, constraints, it) }
            }.map { (unresolvedModule, constraints, substitution) ->
                InferenceDetail(constraints, substitution, unresolvedModule, unresolvedModule.apply(substitution))
            }
}


fun parse(text: String, environment: Environment): Either<Errors, Module> {
    val varPump =
            VarPump()


    return parse(text)
            .andThen { infer2(varPump, it, environment) }
}