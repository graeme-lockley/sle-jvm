package za.co.no9.sle.transform.typedPatternToEnrichedCore

import za.co.no9.sle.*
import za.co.no9.sle.ast.enrichedCore.*
import za.co.no9.sle.ast.enrichedCore.Unit
import za.co.no9.sle.repository.Item
import za.co.no9.sle.typing.*


data class ParseResult(val module: Module, val environment: Environment)


fun parse(source: Item, environment: Environment, callback: ParseCallback): Either<Errors, ParseResult> {
    val typePatternDetail =
            za.co.no9.sle.transform.typelessPatternToTypedPattern.parse(source, environment, callback)

    return typePatternDetail.map {
        callback.environment(it.environment)

        callback.resolvedTypedPatternModule(it.module)

        ParseResult(Transform(it.environment).transform(it.module), it.environment)
    }
}


interface ParseCallback : za.co.no9.sle.transform.typelessPatternToTypedPattern.ParseCallback {
    fun resolvedTypedPatternModule(resolvedTypedPatternModule: za.co.no9.sle.ast.typedPattern.Module)

    fun environment(environment: Environment)
}


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
                    transform(declaration)

                is za.co.no9.sle.ast.typedPattern.TypeAliasDeclaration ->
                    TypeAliasDeclaration(declaration.location, transform(declaration.name), declaration.scheme)

                is za.co.no9.sle.ast.typedPattern.TypeDeclaration ->
                    TypeDeclaration(declaration.location, transform(declaration.name), declaration.scheme, declaration.constructors.map { transform(it) })
            }


    private fun transform(declaration: za.co.no9.sle.ast.typedPattern.LetDeclaration): LetDeclaration =
            LetDeclaration(declaration.location, declaration.scheme, transform(declaration.id), transform(declaration.expressions))


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
                    Unit(expression.location, transform(expression.type))

                is za.co.no9.sle.ast.typedPattern.ConstantBool ->
                    ConstantBool(expression.location, transform(expression.type), expression.value)

                is za.co.no9.sle.ast.typedPattern.ConstantInt ->
                    ConstantInt(expression.location, transform(expression.type), expression.value)

                is za.co.no9.sle.ast.typedPattern.ConstantString ->
                    ConstantString(expression.location, transform(expression.type), expression.value)

                is za.co.no9.sle.ast.typedPattern.ConstantChar ->
                    ConstantChar(expression.location, transform(expression.type), expression.value)

                is za.co.no9.sle.ast.typedPattern.ConstantRecord -> {
                    val expressionType =
                            transform(expression.type)

                    val fieldIndex =
                            expression.fields.fold(mapOf<String, Expression>()) { a, b ->
                                a + Pair(b.name.name, transform(b.value))
                            }

                    val recordType =
                            resolveAlias(environment, expression.type)!!

                    val sortedFieldNames =
                            (recordType as TRec).fields.map { it.first }.sorted()

                    val fields =
                            sortedFieldNames.map { name -> fieldIndex.getValue(name) }

                    ConstantConstructor(expression.location, expressionType, "Tuple${fields.size}", fields)
                }

                is za.co.no9.sle.ast.typedPattern.IdReference ->
                    IdReference(expression.location, transform(expression.type), expression.name)

                is za.co.no9.sle.ast.typedPattern.LetExpression ->
                    LetExpression(expression.location, transform(expression.type), expression.declarations.map { transform(it) }, transform(expression.expression))

                is za.co.no9.sle.ast.typedPattern.IfExpression ->
                    IfExpression(expression.location, transform(expression.type), transform(expression.guardExpression), transform(expression.thenExpression), transform(expression.elseExpression))

                is za.co.no9.sle.ast.typedPattern.LambdaExpression -> {
                    val patternTransformation =
                            transform(expression.argument, PatternTransformState(environment))

                    if (patternTransformation.state.expression == null) {
                        LambdaExpression(expression.location, transform(expression.type), patternTransformation.result, transform(expression.expression))
                    } else {
                        LambdaExpression(
                                expression.location,
                                transform(expression.type),
                                patternTransformation.result,
                                IfExpression(
                                        expression.location,
                                        transform(expression.expression.type),
                                        patternTransformation.state.expression,
                                        transform(expression.expression),
                                        FAIL(expression.location, transform(expression.expression.type))))
                    }
                }

                is za.co.no9.sle.ast.typedPattern.CallExpression ->
                    CallExpression(expression.location, transform(expression.type), transform(expression.operator), transform(expression.operand))

                is za.co.no9.sle.ast.typedPattern.FieldProjectionExpression -> {
                    val recordType =
                            resolveAlias(environment, expression.record.type)!!

                    val sortedFieldNames =
                            (recordType as TRec).fields.map { it.first }.sorted()

                    ProjectionExpression(expression.location, transform(expression.type), transform(expression.record), sortedFieldNames.indexOf(expression.name.name))
                }

                is za.co.no9.sle.ast.typedPattern.UpdateRecordExpression -> {
                    val expressionType =
                            transform(expression.type)

                    val fieldIndex =
                            expression.updates.fold(mapOf<String, Expression>()) { a, b ->
                                a + Pair(b.first.name, transform(b.second))
                            }


                    val recordType =
                            resolveAlias(environment, expression.record.type)!!

                    val sortedFieldNames =
                            (recordType as TRec).fields.map { it.first }.sorted()

                    val fieldTypes =
                            recordType.fields.fold(emptyMap<String, Type>()) { a, b ->
                                a + b.mapSecond { transform(it) }
                            }

                    val record =
                            transform(expression.record)

                    val fields =
                            sortedFieldNames.mapIndexed { index, name ->
                                fieldIndex[name]
                                        ?: ProjectionExpression(expression.location, fieldTypes.getValue(name), record, index)
                            }

                    ConstantConstructor(expression.location, expressionType, "Tuple${fields.size}", fields)
                }

                is za.co.no9.sle.ast.typedPattern.CaseExpression -> {
                    val operator =
                            transform(expression.operator)

                    val caseItemType =
                            TArr(transform(operator.type), transform(expression.type))

                    fun transform(caseItem: za.co.no9.sle.ast.typedPattern.CaseItem): Expression {
                        val patternTransformation =
                                transform(caseItem.pattern, PatternTransformState(environment))

                        val caseItemExpression: Expression =
                                if (patternTransformation.state.expression == null) {
                                    transform(caseItem.expression)
                                } else {
                                    IfExpression(caseItem.location, transform(caseItem.expression.type), patternTransformation.state.expression, transform(caseItem.expression), FAIL(caseItem.location, transform(caseItem.expression.type)))
                                }

                        return CallExpression(caseItem.location, transform(expression.type),
                                LambdaExpression(caseItem.location, caseItemType, patternTransformation.result, caseItemExpression),
                                IdReference(expression.location, transform(operator.type), "\$case"))
                    }

                    val initial: Expression =
                            ERROR(expression.location, transform(expression.type))

                    CallExpression(expression.location, transform(expression.type),
                            LambdaExpression(expression.location, TArr(transform(operator.type), transform(expression.type)), IdReferencePattern(expression.location, transform(operator.type), "\$case"),
                                    Bar(expression.location, transform(expression.type), expression.items.map { b -> transform(b) } + initial)),
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


    private fun transform(pattern: za.co.no9.sle.ast.typedPattern.Pattern, state: PatternTransformState): PatternTransformResult<Pattern> {
        val patternType =
                transform(pattern.type)

        return when (pattern) {
            is za.co.no9.sle.ast.typedPattern.ConstantIntPattern ->
                state.addComparison(pattern.location, patternType, ConstantInt(pattern.location, patternType, pattern.value))

            is za.co.no9.sle.ast.typedPattern.ConstantBoolPattern ->
                state.addComparison(pattern.location, patternType, ConstantBool(pattern.location, patternType, pattern.value))

            is za.co.no9.sle.ast.typedPattern.ConstantStringPattern ->
                state.addComparison(pattern.location, patternType, ConstantString(pattern.location, patternType, pattern.value))

            is za.co.no9.sle.ast.typedPattern.ConstantCharPattern ->
                state.addComparison(pattern.location, patternType, ConstantChar(pattern.location, patternType, pattern.value))

            is za.co.no9.sle.ast.typedPattern.ConstantUnitPattern -> {
                val id =
                        state.newID()

                PatternTransformResult(IdReferencePattern(pattern.location, patternType, id.result), id.state)
            }

            is za.co.no9.sle.ast.typedPattern.IdReferencePattern ->
                PatternTransformResult(IdReferencePattern(pattern.location, patternType, pattern.name), state)

            is za.co.no9.sle.ast.typedPattern.IgnorePattern ->
                PatternTransformResult(IgnorePattern(pattern.location, patternType), state)

            is za.co.no9.sle.ast.typedPattern.ConstructorReferencePattern -> {
                val initialState =
                        PatternTransformResult(emptyList<Pattern>(), state)

                val parameters =
                        pattern.parameters.fold(initialState) { a, b ->
                            val transformResult =
                                    transform(b, a.state)

                            PatternTransformResult(a.result + transformResult.result, transformResult.state)
                        }

                PatternTransformResult(ConstructorReferencePattern(pattern.location, patternType, pattern.name, parameters.result), parameters.state)
            }

            is za.co.no9.sle.ast.typedPattern.RecordPattern -> {
                val expressionType =
                        transform(pattern.type)

                val initialState =
                        PatternTransformResult(emptyList<Pair<ID, Pattern>>(), state)

                val fields =
                        pattern.fields.fold(initialState) { a, b ->
                            val transformResult =
                                    transform(b.second, a.state)

                            PatternTransformResult(a.result + Pair(transform(b.first), transformResult.result), transformResult.state)
                        }

                val fieldIndex =
                        fields.result.fold(mapOf<String, Pattern>()) { a, b ->
                            a + Pair(b.first.name, b.second)
                        }

                val recordType =
                        resolveAlias(environment, pattern.type)!!

                val sortedFieldNames =
                        (recordType as TRec).fields.map { it.first }.sorted()

                val theFields =
                        sortedFieldNames.map { name -> fieldIndex.getValue(name) }

                val tupleName =
                        "Tuple${fields.result.size}"

                val tupleTypeBinding =
                        environment.type(tupleName)

                val constructorName =
                        when (tupleTypeBinding) {
                            is ImportADTBinding ->
                                tupleTypeBinding.item.resolveConstructor(tupleName)

                            else ->
                                tupleName
                        }

                PatternTransformResult(ConstructorReferencePattern(pattern.location, expressionType, constructorName, theFields), fields.state)
            }
        }
    }


    private fun transform(type: Type): Type =
            when (type) {
                is TCon ->
                    if (type.arguments.isEmpty())
                        type
                    else
                        TCon(type.location, type.name, type.arguments.map { transform(it) })

                is TArr ->
                    TArr(type.location, transform(type.domain), transform(type.range))

                is TVar ->
                    type

                is TAlias -> {
                    val ntype =
                            resolveAlias(environment, type)

                    if (ntype == null) {
                        type
                    } else
                        transform(ntype)
                }

                is TRec -> {
                    val fieldIndex =
                            type.fields.fold(emptyMap<String, Type>()) { a, b ->
                                a + b.mapSecond { transform(it) }
                            }

                    val fieldTypes =
                            fieldIndex.keys.toSortedSet().toList().map { fieldIndex.getValue(it) }

                    TCon(type.location, "Tuple${fieldTypes.size}", fieldTypes)
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


