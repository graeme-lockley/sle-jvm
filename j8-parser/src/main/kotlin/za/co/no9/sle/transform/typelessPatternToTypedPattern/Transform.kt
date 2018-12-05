package za.co.no9.sle.transform.typelessPatternToTypedPattern

import za.co.no9.sle.Either
import za.co.no9.sle.Errors
import za.co.no9.sle.andThen
import za.co.no9.sle.ast.typedPattern.Module
import za.co.no9.sle.map
import za.co.no9.sle.repository.Item
import za.co.no9.sle.transform.typelessToTypelessPattern.parse
import za.co.no9.sle.typing.Environment
import za.co.no9.sle.typing.Substitution
import za.co.no9.sle.typing.VarPump


data class InferenceDetail(
        val environment: Environment,
        val constraints: Constraints,
        val substitution: Substitution,
        val unresolvedModule: Module,
        val resolvedModule: Module)


private data class Tuple4<out A, out B, out C, out D>(
         val v1: A,
         val v2: B,
         val v3: C,
         val v4: D)


fun parseWithDetail(source: Item, __env: Environment): Either<Errors, InferenceDetail> {
    val varPump =
            VarPump()

    return parse(source.sourceCode())
            .andThen { infer(source, varPump, it, __env) }
            .andThen { (unresolvedModule, constraints, environment) ->
                unifies(varPump, constraints, environment).map { Tuple4(unresolvedModule, constraints, it, environment) }
            }.andThen { (unresolvedModule, constraints, substitution, environment) ->
                val resolveModule =
                        unresolvedModule.apply(environment, substitution)

                resolveModule.map { InferenceDetail(environment, constraints, substitution, unresolvedModule, it) }
            }
}


fun parse(source: Item, environment: Environment): Either<Errors, InferResult> {
    val varPump =
            VarPump()


    return parse(source.sourceCode())
            .andThen { infer(source, varPump, it, environment) }
}