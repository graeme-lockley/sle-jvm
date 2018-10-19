package za.co.no9.sle.transform.typedPatternToEnrichedCore

import za.co.no9.sle.Either
import za.co.no9.sle.Errors
import za.co.no9.sle.Location
import za.co.no9.sle.ast.enrichedCore.*
import za.co.no9.sle.ast.enrichedCore.Unit
import za.co.no9.sle.map
import za.co.no9.sle.transform.typelessPatternToTypedPattern.Constraints
import za.co.no9.sle.typing.*


data class Detail(
        val constraints: Constraints,
        val substitution: Substitution,
        val unresolvedModule: za.co.no9.sle.ast.typedPattern.Module,
        val resolvedModule: za.co.no9.sle.ast.typedPattern.Module,
        val enrichedModule: Module)


fun parseWithDetail(text: String, environment: Environment): Either<Errors, Detail> {
    val typePatternDetail =
            za.co.no9.sle.transform.typelessPatternToTypedPattern.parseWithDetail(text, environment)

    return typePatternDetail.map {
        Detail(it.constraints, it.substitution, it.unresolvedModule, it.resolvedModule, transform(it.resolvedModule))
    }
}


fun parse(text: String, environment: Environment): Either<Errors, Module> {
    return za.co.no9.sle.transform.typelessPatternToTypedPattern.parse(text, environment).map { transform(it) }
}


private fun transform(module: za.co.no9.sle.ast.typedPattern.Module): Module =
        Module(module.location, module.declarations.map { transform(it) })


private fun transform(declaration: za.co.no9.sle.ast.typedPattern.Declaration): Declaration =
        when (declaration) {
            is za.co.no9.sle.ast.typedPattern.LetDeclaration ->
                LetDeclaration(declaration.location, declaration.scheme, transform(declaration.name), transform(declaration.expression))

            is za.co.no9.sle.ast.typedPattern.TypeAliasDeclaration ->
                TypeAliasDeclaration(declaration.location, transform(declaration.name), declaration.scheme)

            is za.co.no9.sle.ast.typedPattern.TypeDeclaration ->
                TypeDeclaration(declaration.location, transform(declaration.name), declaration.scheme, declaration.constructors.map { transform(it) })
        }


private fun transform(expression: za.co.no9.sle.ast.typedPattern.Expression): Expression =
        when (expression) {
            is za.co.no9.sle.ast.typedPattern.Unit ->
                Unit(expression.location, expression.type)

            is za.co.no9.sle.ast.typedPattern.ConstantBool ->
                ConstantBool(expression.location, expression.type, expression.value)

            is za.co.no9.sle.ast.typedPattern.ConstantInt ->
                ConstantInt(expression.location, expression.type, expression.value)

            is za.co.no9.sle.ast.typedPattern.ConstantString ->
                ConstantString(expression.location, expression.type, expression.value)

            is za.co.no9.sle.ast.typedPattern.IdReference ->
                IdReference(expression.location, expression.type, expression.name)

            is za.co.no9.sle.ast.typedPattern.IfExpression ->
                IfExpression(expression.location, expression.type, transform(expression.guardExpression), transform(expression.thenExpression), transform(expression.elseExpression))

            is za.co.no9.sle.ast.typedPattern.LambdaExpression ->
                LambdaExpression(expression.location, expression.type, IdReferencePattern(expression.argument.location, domain(expression.type), expression.argument.name), transform(expression.expression))

            is za.co.no9.sle.ast.typedPattern.CallExpression ->
                CallExpression(expression.location, expression.type, transform(expression.operator), transform(expression.operand))

            is za.co.no9.sle.ast.typedPattern.CaseExpression -> {
                val operator =
                        transform(expression.operator)

                val caseItemType =
                        TArr(operator.type, expression.type)

                fun transform(caseItem: za.co.no9.sle.ast.typedPattern.CaseItem): Expression {
                    val patternTransformation =
                            transform(caseItem.pattern, PatternTransformState())

                    val caseItemExpression: Expression =
                            if (patternTransformation.state.expression == null) {
                                transform(caseItem.expression)
                            } else {
                                IfExpression(caseItem.location, caseItem.expression.type, patternTransformation.state.expression, transform(caseItem.expression), FAIL(caseItem.location, caseItem.expression.type))
                            }

                    return CallExpression(caseItem.location, expression.type,
                            LambdaExpression(caseItem.location, caseItemType, patternTransformation.result, caseItemExpression),
                            IdReference(expression.location, operator.type, "\$case"))
                }

                val initial: Expression =
                        ERROR(expression.location, expression.type)

                CallExpression(expression.location, expression.type,
                        LambdaExpression(expression.location, TArr(operator.type, expression.type), IdReferencePattern(expression.location, operator.type, "\$case"),
                                expression.items.foldRight(initial) { a, b -> Bar(a.location + b.location, caseItemType, transform(a), b) }),
                        operator
                )
            }
        }


private class PatternTransformState(val count: Int = 0, val expression: Expression? = null) {
    fun newID(): PatternTransformResult<String> {
        val id =
                "\$v$count"

        return PatternTransformResult(id, PatternTransformState(count + 1, expression))
    }

    private fun addExpression(expression: Expression): PatternTransformState =
            if (this.expression == null)
                PatternTransformState(count, expression)
            else
                PatternTransformState(count, CallExpression(
                        expression.location,
                        typeBool,
                        CallExpression(
                                expression.location + this.expression.location,
                                TArr(typeBool, typeBool),
                                IdReference(expression.location, TArr(typeBool, TArr(typeBool, typeBool)), "(&&)"),
                                this.expression),
                        expression

                ))

    fun addComparison(location: Location, type: Type, constantExpression: Expression): PatternTransformResult<Pattern> {
        val id =
                this.newID()

        val finalState =
                id.state.addExpression(
                        CallExpression(
                                location,
                                typeBool,
                                CallExpression(
                                        location,
                                        TArr(type, typeBool),
                                        IdReference(location, TArr(type, TArr(type, typeBool)), "(==)"),
                                        constantExpression),
                                IdReference(location, type, id.result)))

        return PatternTransformResult(IdReferencePattern(location, type, id.result), finalState)
    }
}


private class PatternTransformResult<T>(val result: T, val state: PatternTransformState)


private fun transform(pattern: za.co.no9.sle.ast.typedPattern.Pattern, state: PatternTransformState): PatternTransformResult<Pattern> =
        when (pattern) {
            is za.co.no9.sle.ast.typedPattern.ConstantIntPattern ->
                state.addComparison(pattern.location, pattern.type, ConstantInt(pattern.location, pattern.type, pattern.value))

            is za.co.no9.sle.ast.typedPattern.ConstantBoolPattern ->
                state.addComparison(pattern.location, pattern.type, ConstantBool(pattern.location, pattern.type, pattern.value))

            is za.co.no9.sle.ast.typedPattern.ConstantStringPattern ->
                state.addComparison(pattern.location, pattern.type, ConstantString(pattern.location, pattern.type, pattern.value))

            is za.co.no9.sle.ast.typedPattern.ConstantUnitPattern -> {
                val id =
                        state.newID()

                PatternTransformResult(IdReferencePattern(pattern.location, pattern.type, id.result), id.state)
            }

            is za.co.no9.sle.ast.typedPattern.IdReferencePattern ->
                PatternTransformResult(IdReferencePattern(pattern.location, pattern.type, pattern.name), state)

            is za.co.no9.sle.ast.typedPattern.ConstructorReferencePattern -> {
                val initialState =
                        PatternTransformResult(emptyList<Pattern>(), state)

                val parameters =
                        pattern.parameters.fold(initialState) { a, b ->
                            val transformResult =
                                    transform(b, a.state)

                            PatternTransformResult(a.result + transformResult.result, transformResult.state)
                        }

                PatternTransformResult(ConstructorReferencePattern(pattern.location, pattern.type, pattern.name, parameters.result), parameters.state)
            }
        }


private fun domain(type: Type): Type =
        when (type) {
            is TArr ->
                type.domain

            else ->
                type
        }


private fun transform(name: za.co.no9.sle.ast.typedPattern.ID): ID =
        ID(name.location, name.name)


private fun transform(constructor: za.co.no9.sle.ast.typedPattern.Constructor): Constructor =
        Constructor(constructor.location, transform(constructor.name), constructor.arguments)


