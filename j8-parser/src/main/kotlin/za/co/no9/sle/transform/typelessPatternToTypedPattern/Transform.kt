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


fun parse(source: Item, environment: Environment, callback: ParseCallback): Either<Errors, InferResult> {
    val varPump =
            VarPump()


    return source.sourceCode()
            .andThen { parse(it) }
            .andThen { infer(source, varPump, it, environment) }
            .andThen { inferResult ->
                callback.unresolvedTypedPatternModule(inferResult.module)
                callback.constraints(inferResult.constraints)

                unifies(varPump, inferResult.constraints, inferResult.environment)
                        .andThen { substitution ->
                            callback.substitution(substitution)

                            inferResult.module.apply(inferResult.environment, substitution)
                                    .map { resolvedModule ->
                                        InferResult(resolvedModule, inferResult.constraints, inferResult.environment)
                                    }
                        }
            }
}


interface ParseCallback {
    fun unresolvedTypedPatternModule(unresolvedTypedPatternModule: Module)

    fun constraints(constraints: Constraints)

    fun substitution(substitution: Substitution)
}