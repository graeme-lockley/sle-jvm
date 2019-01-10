package za.co.no9.sle.transform.typedPatternToEnrichedCore

import za.co.no9.sle.Either
import za.co.no9.sle.Errors
import za.co.no9.sle.Location
import za.co.no9.sle.ast.enrichedCore.*
import za.co.no9.sle.ast.enrichedCore.Unit
import za.co.no9.sle.map
import za.co.no9.sle.repository.Item
import za.co.no9.sle.transform.typelessPatternToTypedPattern.Constraints
import za.co.no9.sle.typing.*


data class Detail(
        val environment: Environment,
        val constraints: Constraints,
        val substitution: Substitution,
        val unresolvedModule: za.co.no9.sle.ast.typedPattern.Module,
        val resolvedModule: za.co.no9.sle.ast.typedPattern.Module,
        val enrichedModule: Module)


fun parseWithDetail(source: Item, environment: Environment): Either<Errors, Detail> {
    val typePatternDetail =
            za.co.no9.sle.transform.typelessPatternToTypedPattern.parseWithDetail(source, environment)

    return typePatternDetail.map {
        Detail(it.environment, it.constraints, it.substitution, it.unresolvedModule, it.resolvedModule, Transform(environment).transform(it.resolvedModule))
    }
}


fun parse(source: Item, environment: Environment): Either<Errors, Module> =
        za.co.no9.sle.transform.typelessPatternToTypedPattern.parse(source, environment).map { Transform(environment).transform(it.module) }


private class Transform(val environment: Environment) {
    fun transform(module: za.co.no9.sle.ast.typedPattern.Module): Module =
            Module(module.location, module.exports.map { transform(it) }, module.declarations.map { transform(it) })


    private fun transform(exportDeclaration: za.co.no9.sle.ast.typedPattern.ExportDeclaration): NameDeclaration =
            when (exportDeclaration) {
                is za.co.no9.sle.ast.typedPattern.ValueExportDeclaration ->
                    ValueNameDeclaration(exportDeclaration.name, exportDeclaration.scheme)

                is za.co.no9.sle.ast.typedPattern.OperatorExportDeclaration ->
                    OperatorNameDeclaration(exportDeclaration.name, exportDeclaration.scheme, exportDeclaration.precedence, exportDeclaration.associativity)

                is za.co.no9.sle.ast.typedPattern.AliasExportDeclaration ->
                    AliasNameDeclaration(exportDeclaration.name, exportDeclaration.scheme)

                is za.co.no9.sle.ast.typedPattern.ADTExportDeclaration ->
                    ADTNameDeclaration(exportDeclaration.name, exportDeclaration.scheme)

                is za.co.no9.sle.ast.typedPattern.FullADTExportDeclaration ->
                    FullADTNameDeclaration(exportDeclaration.name, exportDeclaration.scheme, exportDeclaration.constructors.map { ConstructorNameDeclaration(it.name, it.scheme) })
            }

    private fun transform(declaration: za.co.no9.sle.ast.typedPattern.Declaration): Declaration =
            when (declaration) {
                is za.co.no9.sle.ast.typedPattern.LetDeclaration ->
                    LetDeclaration(declaration.location, declaration.scheme, transform(declaration.id), transform(declaration.expressions))

                is za.co.no9.sle.ast.typedPattern.TypeAliasDeclaration ->
                    TypeAliasDeclaration(declaration.location, transform(declaration.name), declaration.scheme)

                is za.co.no9.sle.ast.typedPattern.TypeDeclaration ->
                    TypeDeclaration(declaration.location, transform(declaration.name), declaration.scheme, declaration.constructors.map { transform(it) })
            }


    private fun transform(expressions: List<za.co.no9.sle.ast.typedPattern.Expression>): Expression =
            if (expressions.size == 1) {
                transform(expressions[0])
            } else {
                extractLambdas(expressions)
            }


    private fun extractLambdas(expressions: List<za.co.no9.sle.ast.typedPattern.Expression>): Expression {
        val numberOfLambdas =
                lambdaDepth(expressions)

        fun variableName(count: Int) =
                "\$v$count"

        fun wrapExpressionWithCall(expression: Expression, count: Int, type: Type): Expression =
                if (count == numberOfLambdas)
                    expression
                else
                    wrapExpressionWithCall(
                            CallExpression(expression.location, range(type),
                                    expression,
                                    IdReference(expression.location, domain(type), variableName(numberOfLambdas - count - 1))),
                            count + 1,
                            range(type))


        fun assembleExpression(expression: Expression, count: Int, type: Type): Expression =
                if (count == numberOfLambdas)
                    expression
                else
                    LambdaExpression(
                            expression.location,
                            type,
                            IdReferencePattern(expression.location, domain(type), variableName(count)),
                            assembleExpression(expression, count + 1, range(type))
                    )

        val declarationType =
                expressions[0].type

        val location =
                expressions.drop(1).fold(expressions[0].location) { a, b -> a + b.location }

        return assembleExpression(
                Bar(
                        location,
                        declarationType,
                        expressions.map { wrapExpressionWithCall(transform(it), 0, declarationType) } + ERROR(location, declarationType)),
                0,
                declarationType)
    }


    private fun lambdaDepth(expressions: List<za.co.no9.sle.ast.typedPattern.Expression>): Int {
        val allLambdas =
                expressions.fold(true) { a, b -> a && b is za.co.no9.sle.ast.typedPattern.LambdaExpression }

        return if (allLambdas) {
            1 + lambdaDepth(expressions.map { (it as za.co.no9.sle.ast.typedPattern.LambdaExpression).expression })
        } else {
            0
        }
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

                is za.co.no9.sle.ast.typedPattern.ConstantChar ->
                    ConstantChar(expression.location, expression.type, expression.value)

                is za.co.no9.sle.ast.typedPattern.IdReference ->
                    IdReference(expression.location, expression.type, expression.name)

                is za.co.no9.sle.ast.typedPattern.IfExpression ->
                    IfExpression(expression.location, expression.type, transform(expression.guardExpression), transform(expression.thenExpression), transform(expression.elseExpression))

                is za.co.no9.sle.ast.typedPattern.LambdaExpression -> {
                    val patternTransformation =
                            transform(expression.argument, PatternTransformState(environment))

                    if (patternTransformation.state.expression == null) {
                        LambdaExpression(expression.location, expression.type, patternTransformation.result, transform(expression.expression))
                    } else {
                        LambdaExpression(
                                expression.location,
                                expression.type,
                                patternTransformation.result,
                                IfExpression(
                                        expression.location,
                                        expression.expression.type,
                                        patternTransformation.state.expression,
                                        transform(expression.expression),
                                        FAIL(expression.location, expression.expression.type)))
                    }
                }

                is za.co.no9.sle.ast.typedPattern.CallExpression ->
                    CallExpression(expression.location, expression.type, transform(expression.operator), transform(expression.operand))

                is za.co.no9.sle.ast.typedPattern.CaseExpression -> {
                    val operator =
                            transform(expression.operator)

                    val caseItemType =
                            TArr(operator.type, expression.type)

                    fun transform(caseItem: za.co.no9.sle.ast.typedPattern.CaseItem): Expression {
                        val patternTransformation =
                                transform(caseItem.pattern, PatternTransformState(environment))

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
                                    Bar(expression.location, expression.type, expression.items.map { b -> transform(b) } + initial)),
                            operator
                    )
                }
            }


    private class PatternTransformState(val environment: Environment, val count: Int = 0, val expression: Expression? = null) {
        fun newID(): PatternTransformResult<String> {
            val id =
                    "\$v$count"

            return PatternTransformResult(id, PatternTransformState(environment, count + 1, expression))
        }

        private fun addExpression(expression: Expression): PatternTransformState =
                if (this.expression == null)
                    PatternTransformState(environment, count, expression)
                else
                    PatternTransformState(environment, count, CallExpression(
                            expression.location,
                            typeBool,
                            CallExpression(
                                    expression.location + this.expression.location,
                                    TArr(typeBool, typeBool),
                                    IdReference(expression.location, TArr(typeBool, TArr(typeBool, typeBool)), "&&"),
                                    this.expression),
                            expression

                    ))

        fun addComparison(location: Location, type: Type, constantExpression: Expression): PatternTransformResult<Pattern> {
            val id =
                    this.newID()

            val equalEqualBinding =
                    environment.value("==")

            val equalEqual =
                    when (equalEqualBinding) {
                        is ImportOperatorBinding ->
                            equalEqualBinding.item.resolveId("==")

                        null ->
                            "=="

                        else ->
                            "=="
                    }

            val finalState =
                    id.state.addExpression(
                            CallExpression(
                                    location,
                                    typeBool,
                                    CallExpression(
                                            location,
                                            TArr(type, typeBool),
                                            IdReference(location, TArr(type, TArr(type, typeBool)), equalEqual),
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

                is za.co.no9.sle.ast.typedPattern.ConstantCharPattern ->
                    state.addComparison(pattern.location, pattern.type, ConstantChar(pattern.location, pattern.type, pattern.value))

                is za.co.no9.sle.ast.typedPattern.ConstantUnitPattern -> {
                    val id =
                            state.newID()

                    PatternTransformResult(IdReferencePattern(pattern.location, pattern.type, id.result), id.state)
                }

                is za.co.no9.sle.ast.typedPattern.IdReferencePattern ->
                    PatternTransformResult(IdReferencePattern(pattern.location, pattern.type, pattern.name), state)

                is za.co.no9.sle.ast.typedPattern.IgnorePattern ->
                    PatternTransformResult(IgnorePattern(pattern.location, pattern.type), state)

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
}

private fun domain(type: Type): Type =
        when (type) {
            is TArr ->
                type.domain

            else ->
                type
        }


private fun range(type: Type): Type =
        when (type) {
            is TArr ->
                type.range

            else ->
                type
        }


private fun transform(id: za.co.no9.sle.ast.typedPattern.ValueDeclarationID): ValueDeclarationID =
        when (id) {
            is za.co.no9.sle.ast.typedPattern.LowerIDDeclarationID ->
                LowerIDDeclarationID(id.location, transform(id.name))

            is za.co.no9.sle.ast.typedPattern.OperatorDeclarationID ->
                OperatorDeclarationID(id.location, transform(id.name), id.precedence, id.associativity)
        }


private fun transform(name: za.co.no9.sle.ast.typedPattern.ID): ID =
        ID(name.location, name.name)


private fun transform(constructor: za.co.no9.sle.ast.typedPattern.Constructor): Constructor =
        Constructor(constructor.location, transform(constructor.name), constructor.arguments)


