package za.co.no9.sle.transform.enrichedCoreToCore

import za.co.no9.sle.*
import za.co.no9.sle.ast.core.*
import za.co.no9.sle.ast.core.Unit
import za.co.no9.sle.repository.Item
import za.co.no9.sle.transform.typelessPatternToTypedPattern.Constraints
import za.co.no9.sle.typing.*


private typealias Patterns =
        List<za.co.no9.sle.ast.enrichedCore.Pattern>

private typealias Q =
        Pair<Patterns, za.co.no9.sle.ast.enrichedCore.Expression>

private typealias Qs =
        List<Q>

data class ParseDetail(val environment: Environment, val module: Module)


fun parse(source: Item, environment: Environment, callback: ParseCallback = EmptyParseCallback()): Either<Errors, ParseDetail> {
    val typePatternDetail =
            za.co.no9.sle.transform.typedPatternToEnrichedCore.parse(source, environment, callback)

    return typePatternDetail.andThen { parseResult ->
        callback.enrichedCoreModule(parseResult.module)

        transform(parseResult.environment, parseResult.module)
                .map { ParseDetail(parseResult.environment, it) }
    }
}


interface ParseCallback : za.co.no9.sle.transform.typedPatternToEnrichedCore.ParseCallback {
    fun enrichedCoreModule(enrichedCoreModule: za.co.no9.sle.ast.enrichedCore.Module)
}


class EmptyParseCallback : za.co.no9.sle.transform.enrichedCoreToCore.ParseCallback {
    override fun resolvedTypedPatternModule(resolvedTypedPatternModule: za.co.no9.sle.ast.typedPattern.Module) {
    }

    override fun unresolvedTypedPatternModule(unresolvedTypedPatternModule: za.co.no9.sle.ast.typedPattern.Module) {
    }

    override fun enrichedCoreModule(enrichedCoreModule: za.co.no9.sle.ast.enrichedCore.Module) {
    }

    override fun constraints(constraints: Constraints) {
    }

    override fun substitution(substitution: Substitution) {
    }

    override fun environment(environment: Environment) {
    }
}


private fun transform(environment: Environment, module: za.co.no9.sle.ast.enrichedCore.Module): Either<Errors, Module> {
    val transform =
            Transform(environment)

    val m =
            transform.transform(module)

    return if (transform.errors.isEmpty())
        value(m)
    else
        error(transform.errors)
}


private class Transform(val environment: Environment, private var counter: Int = 0) {
    var constructors: Map<String, List<String>> =
            emptyMap()

    val errors =
            mutableSetOf<za.co.no9.sle.Error>()


    fun transform(module: za.co.no9.sle.ast.enrichedCore.Module): Module {
        constructors = constructors(environment)

        return Module(module.location, module.exports.map { transform(it) }, module.declarations.map { transform(it) })
    }


    private fun transform(nameDeclaration: za.co.no9.sle.ast.enrichedCore.NameDeclaration): NameDeclaration =
            when (nameDeclaration) {
                is za.co.no9.sle.ast.enrichedCore.ValueNameDeclaration ->
                    ValueNameDeclaration(nameDeclaration.name, nameDeclaration.scheme)

                is za.co.no9.sle.ast.enrichedCore.OperatorNameDeclaration ->
                    OperatorNameDeclaration(nameDeclaration.name, nameDeclaration.scheme, nameDeclaration.precedence, nameDeclaration.associativity)

                is za.co.no9.sle.ast.enrichedCore.AliasNameDeclaration ->
                    AliasNameDeclaration(nameDeclaration.name, nameDeclaration.scheme)

                is za.co.no9.sle.ast.enrichedCore.ADTNameDeclaration ->
                    ADTNameDeclaration(nameDeclaration.name, nameDeclaration.scheme)

                is za.co.no9.sle.ast.enrichedCore.FullADTNameDeclaration ->
                    FullADTNameDeclaration(nameDeclaration.name, nameDeclaration.scheme, nameDeclaration.constructors.map { ConstructorNameDeclaration(it.name, it.scheme) })
            }


    private fun transform(declaration: za.co.no9.sle.ast.enrichedCore.Declaration): Declaration =
            when (declaration) {
                is za.co.no9.sle.ast.enrichedCore.LetDeclaration ->
                    transform(declaration)

                is za.co.no9.sle.ast.enrichedCore.TypeAliasDeclaration ->
                    TypeAliasDeclaration(declaration.location, transform(declaration.name), declaration.scheme)

                is za.co.no9.sle.ast.enrichedCore.TypeDeclaration ->
                    TypeDeclaration(declaration.location, transform(declaration.name), declaration.scheme, declaration.constructors.map { transform(it) })
            }


    private fun transform(declaration: za.co.no9.sle.ast.enrichedCore.LetDeclaration): LetDeclaration =
            LetDeclaration(declaration.location, declaration.scheme, transform(declaration.id), transform(declaration.expression))


    private fun transform(expression: za.co.no9.sle.ast.enrichedCore.Expression): Expression =
            when (expression) {
                is za.co.no9.sle.ast.enrichedCore.Unit ->
                    Unit(expression.location, expression.type)

                is za.co.no9.sle.ast.enrichedCore.ConstantBool ->
                    ConstantBool(expression.location, expression.type, expression.value)

                is za.co.no9.sle.ast.enrichedCore.ConstantInt ->
                    ConstantInt(expression.location, expression.type, expression.value)

                is za.co.no9.sle.ast.enrichedCore.ConstantString ->
                    ConstantString(expression.location, expression.type, expression.value)

                is za.co.no9.sle.ast.enrichedCore.ConstantChar ->
                    ConstantChar(expression.location, expression.type, expression.value)

                is za.co.no9.sle.ast.enrichedCore.ConstantRecord ->
                    ConstantRecord(expression.location, expression.type, expression.fields.map { ConstantField(it.location, transform(it.name), transform(it.value)) })

                is za.co.no9.sle.ast.enrichedCore.FAIL ->
                    FAIL(expression.location, expression.type)

                is za.co.no9.sle.ast.enrichedCore.ERROR ->
                    ERROR(expression.location, expression.type)

                is za.co.no9.sle.ast.enrichedCore.IdReference ->
                    IdReference(expression.location, expression.type, expression.name)

                is za.co.no9.sle.ast.enrichedCore.LetExpression ->
                    LetExpression(expression.location, expression.type, expression.declarations.map { transform(it) }, transform(expression.expression))

                is za.co.no9.sle.ast.enrichedCore.IfExpression ->
                    IfExpression(expression.location, expression.type, transform(expression.guardExpression), transform(expression.thenExpression), transform(expression.elseExpression))

                is za.co.no9.sle.ast.enrichedCore.LambdaExpression -> {
                    val argument =
                            expression.argument

                    if (argument is za.co.no9.sle.ast.enrichedCore.ConstructorReferencePattern) {
                        val name =
                                variable()

                        LambdaExpression(expression.location, expression.type,
                                ID(expression.argument.location, name),
                                CaseExpression(expression.location, expression.type, name, listOf(
                                        CaseExpressionClause(
                                                argument.name,
                                                argument.parameters.map {
                                                    when (it) {
                                                        is za.co.no9.sle.ast.enrichedCore.IdReferencePattern ->
                                                            it.name

                                                        else ->
                                                            "_"
                                                    }
                                                },
                                                transform(expression.expression))
                                ))
                        )
                    } else
                        LambdaExpression(expression.location, expression.type, transform(expression.argument), transform(expression.expression))
                }

                is za.co.no9.sle.ast.enrichedCore.CallExpression ->
                    CallExpression(expression.location, expression.type, transform(expression.operator), transform(expression.operand))

                is za.co.no9.sle.ast.enrichedCore.FieldProjectionExpression ->
                    FieldProjectionExpression(expression.location, expression.type, transform(expression.record), transform(expression.name))

                is za.co.no9.sle.ast.enrichedCore.UpdateRecordExpression -> {
                    val resolveAlias =
                            resolveAlias(environment, expression.record.type)

                    val fields =
                            (resolveAlias as TRec).fields

                    val updates =
                            expression.updates.map { Pair(it.first.name, it.second) }.toMap().toSortedMap()


                    val result = LetExpression(
                            expression.location,
                            expression.type,
                            listOf(LetDeclaration(
                                    expression.record.location,
                                    generalise(expression.record.type),
                                    LowerIDDeclarationID(expression.record.location, ID(expression.record.location, "\$record")),
                                    transform(expression.record))
                            ),
                            ConstantRecord(
                                    expression.location,
                                    expression.record.type,
                                    fields.map {
                                        val update =
                                                updates[it.first]

                                        if (update == null)
                                            ConstantField(expression.location, ID(expression.location, it.first), FieldProjectionExpression(expression.location, it.second, IdReference(expression.location, expression.record.type, "\$record"), ID(expression.location, it.first)))
                                        else
                                            ConstantField(expression.location, ID(expression.location, it.first), transform(update))
                                    }
                            )
                    )

                    result
                }

                is za.co.no9.sle.ast.enrichedCore.Bar -> {
                    fun extractNames(e: za.co.no9.sle.ast.enrichedCore.Expression): List<String> =
                            when (e) {
                                is za.co.no9.sle.ast.enrichedCore.CallExpression -> {
                                    val operand =
                                            e.operand

                                    if (operand is za.co.no9.sle.ast.enrichedCore.IdReference)
                                        listOf(operand.name) + extractNames(e.operator)
                                    else
                                        listOf()
                                }

                                else ->
                                    listOf()
                            }

                    val names =
                            extractNames(expression.expressions[0])

                    fun createQ(e: za.co.no9.sle.ast.enrichedCore.Expression): Q {
                        fun dropNCalls(n: Int, exp: za.co.no9.sle.ast.enrichedCore.Expression): za.co.no9.sle.ast.enrichedCore.Expression =
                                if (n == 0)
                                    exp
                                else if (exp is za.co.no9.sle.ast.enrichedCore.CallExpression)
                                    if (exp.operand is za.co.no9.sle.ast.enrichedCore.IdReference)
                                        dropNCalls(n - 1, exp.operator)
                                    else
                                        throw Error("Error: n > 0 and $exp is not an IdReference")
                                else
                                    throw Error("Error: n == 0 and $exp not a CallExpression")

                        fun extractPatterns(n: Int, exp: za.co.no9.sle.ast.enrichedCore.Expression): Q {
                            var currentE =
                                    exp

                            val patterns =
                                    mutableListOf<za.co.no9.sle.ast.enrichedCore.Pattern>()

                            for (i in 1..n) {
                                if (currentE is za.co.no9.sle.ast.enrichedCore.LambdaExpression) {
                                    patterns.add(currentE.argument)
                                    currentE = currentE.expression
                                } else {
                                    throw Error("Expected $n lambdas during pattern extraction: $exp")
                                }
                            }

                            return Pair(patterns, currentE)
                        }

                        return extractPatterns(names.size, dropNCalls(names.size, e))
                    }


                    val qs = expression.expressions.filter { !isError(it) }.map { createQ(it) }

                    val result =
                            match(names, qs, transform(expression.expressions.filter { isError(it) }[0]))

                    if (hasError(result)) {
                        errors.add(NonExhaustivePattern(expression.location))
                    }

                    result
                }
            }


    private fun hasError(e: Expression): Boolean =
            when (e) {
                is IfExpression ->
                    hasError(e.guardExpression) || hasError(e.thenExpression) || hasError(e.elseExpression)

                is LambdaExpression ->
                    hasError(e.expression)

                is CallExpression ->
                    hasError(e.operand) || hasError(e.operator)

                is CaseExpression ->
                    e.clauses.fold(false) { a, b -> a || hasError(b.expression) }

                is ERROR ->
                    true

                else ->
                    false
            }


    private fun isError(e: za.co.no9.sle.ast.enrichedCore.Expression): Boolean =
            e is za.co.no9.sle.ast.enrichedCore.ERROR


    private fun variable(): String {
        val variable =
                "$$$counter"

        counter += 1

        return variable
    }


    private fun variables(n: Int): List<String> {
        val result =
                mutableListOf<String>()

        for (i in 1..n)
            result.add(variable())

        return result
    }


    fun match(us: List<String>, qs: Qs, e: Expression): Expression =
            if (us.isEmpty()) {
                combineExpressionWithFail(qs.map { it.second }, e)
            } else {
                val firstFirstPattern =
                        qs[0].first[0]

                if (firstFirstPattern is za.co.no9.sle.ast.enrichedCore.ConstructorReferencePattern) {
                    val splitList =
                            qs.splitWhile { it.first[0] is za.co.no9.sle.ast.enrichedCore.ConstructorReferencePattern }

                    val remainderOfClauses =
                            if (splitList.second.isEmpty())
                                e
                            else
                                match(us, splitList.second, e)

                    val clausesGroupedByConstructorName =
                            splitList.first.groupBy { (it.first[0] as za.co.no9.sle.ast.enrichedCore.ConstructorReferencePattern).name }

                    val clausesConstructorNames =
                            clausesGroupedByConstructorName.keys.toList()

                    val constructorNamesNotInGroup =
                            constructors[clausesConstructorNames[0]]!! - clausesConstructorNames

                    val clauses =
                            clausesGroupedByConstructorName.map {
                                val constructorName =
                                        it.key

                                val constructorQs =
                                        it.value

                                val constructor =
                                        constructorQs[0].first[0] as za.co.no9.sle.ast.enrichedCore.ConstructorReferencePattern

                                val names =
                                        variables(constructor.parameters.size)

                                CaseExpressionClause(constructorName, names, match(names + us.drop(1), constructorQs.map {
                                    val constructorReferencePattern =
                                            it.first[0] as za.co.no9.sle.ast.enrichedCore.ConstructorReferencePattern

                                    Pair(constructorReferencePattern.parameters + it.first.drop(1), it.second)
                                }, remainderOfClauses))
                            } + constructorNamesNotInGroup.map { CaseExpressionClause(it, listOf(), remainderOfClauses) }

                    CaseExpression(e.location, e.type, us[0], clauses)
                } else {
                    match(us.drop(1), qs.map {
                        val variable =
                                it.first[0]

                        val variableName =
                                when (variable) {
                                    is za.co.no9.sle.ast.enrichedCore.IdReferencePattern ->
                                        variable.name

                                    else ->
                                        "_"
                                }

                        Pair(it.first.drop(1), substitute(it.second, variableName, us[0]))
                    }, e)
                }
            }


    private fun combineExpressionWithFail(expressions: List<za.co.no9.sle.ast.enrichedCore.Expression>, e: Expression): Expression =
            when {
                expressions.isEmpty() ->
                    e

                canFail(expressions[0]) ->
                    replaceFailWith(transform(expressions[0]), combineExpressionWithFail(expressions.drop(1), e))

                else ->
                    transform(expressions[0])
            }


    private fun replaceFailWith(haystack: Expression, needle: Expression): Expression =
            when (haystack) {
                is Unit ->
                    haystack

                is ConstantBool ->
                    haystack

                is ConstantInt ->
                    haystack

                is ConstantString ->
                    haystack

                is ConstantChar ->
                    haystack

                is ConstantRecord ->
                    ConstantRecord(haystack.location, haystack.type, haystack.fields.map { ConstantField(it.location, it.name, replaceFailWith(it.value, needle)) })

                is FAIL ->
                    needle

                is ERROR ->
                    haystack

                is IdReference ->
                    haystack

                is LetExpression ->
                    LetExpression(haystack.location, haystack.type, haystack.declarations, replaceFailWith(haystack.expression, needle))

                is IfExpression ->
                    IfExpression(haystack.location, haystack.type, replaceFailWith(haystack.guardExpression, needle), replaceFailWith(haystack.thenExpression, needle), replaceFailWith(haystack.elseExpression, needle))

                is LambdaExpression ->
                    LambdaExpression(haystack.location, haystack.type, haystack.argument, replaceFailWith(haystack.expression, needle))

                is CallExpression ->
                    CallExpression(haystack.location, haystack.type, replaceFailWith(haystack.operator, needle), replaceFailWith(haystack.operand, needle))

                is FieldProjectionExpression ->
                    FieldProjectionExpression(haystack.location, haystack.type, replaceFailWith(haystack.record, needle), haystack.name)

                is CaseExpression ->
                    CaseExpression(haystack.location, haystack.type, haystack.variable, haystack.clauses.map { CaseExpressionClause(it.constructorName, it.variables, replaceFailWith(it.expression, needle)) })
            }


    private fun substitute(e: za.co.no9.sle.ast.enrichedCore.Expression, old: String, new: String): za.co.no9.sle.ast.enrichedCore.Expression =
            when (e) {
                is za.co.no9.sle.ast.enrichedCore.Unit ->
                    e

                is za.co.no9.sle.ast.enrichedCore.ConstantBool ->
                    e

                is za.co.no9.sle.ast.enrichedCore.ConstantInt ->
                    e

                is za.co.no9.sle.ast.enrichedCore.ConstantString ->
                    e

                is za.co.no9.sle.ast.enrichedCore.ConstantChar ->
                    e

                is za.co.no9.sle.ast.enrichedCore.ConstantRecord ->
                    za.co.no9.sle.ast.enrichedCore.ConstantRecord(e.location, e.type, e.fields.map { za.co.no9.sle.ast.enrichedCore.ConstantField(it.location, it.name, substitute(it.value, old, new)) })

                is za.co.no9.sle.ast.enrichedCore.FAIL ->
                    e

                is za.co.no9.sle.ast.enrichedCore.ERROR ->
                    e

                is za.co.no9.sle.ast.enrichedCore.IdReference ->
                    if (e.name == old)
                        za.co.no9.sle.ast.enrichedCore.IdReference(e.location, e.type, new)
                    else
                        e
                is za.co.no9.sle.ast.enrichedCore.LetExpression ->
                    za.co.no9.sle.ast.enrichedCore.LetExpression(e.location, e.type, e.declarations.map { substitute(it, old, new) }, substitute(e.expression, old, new))

                is za.co.no9.sle.ast.enrichedCore.IfExpression ->
                    za.co.no9.sle.ast.enrichedCore.IfExpression(e.location, e.type, substitute(e.guardExpression, old, new), substitute(e.thenExpression, old, new), substitute(e.elseExpression, old, new))

                is za.co.no9.sle.ast.enrichedCore.LambdaExpression ->
                    za.co.no9.sle.ast.enrichedCore.LambdaExpression(e.location, e.type, e.argument, substitute(e.expression, old, new))

                is za.co.no9.sle.ast.enrichedCore.CallExpression ->
                    za.co.no9.sle.ast.enrichedCore.CallExpression(e.location, e.type, substitute(e.operator, old, new), substitute(e.operand, old, new))

                is za.co.no9.sle.ast.enrichedCore.FieldProjectionExpression ->
                    za.co.no9.sle.ast.enrichedCore.FieldProjectionExpression(e.location, e.type, substitute(e.record, old, new), e.name)

                is za.co.no9.sle.ast.enrichedCore.UpdateRecordExpression ->
                    za.co.no9.sle.ast.enrichedCore.UpdateRecordExpression(e.location, e.type, substitute(e.record, old, new), e.updates.map { Pair(it.first, substitute(it.second, old, new)) })

                is za.co.no9.sle.ast.enrichedCore.Bar ->
                    za.co.no9.sle.ast.enrichedCore.Bar(e.location, e.type, e.expressions.map { substitute(it, old, new) })
            }


    private fun substitute(e: za.co.no9.sle.ast.enrichedCore.LetDeclaration, old: String, new: String): za.co.no9.sle.ast.enrichedCore.LetDeclaration =
            za.co.no9.sle.ast.enrichedCore.LetDeclaration(e.location, e.scheme, e.id, substitute(e.expression, old, new))


    private fun canFail(e: za.co.no9.sle.ast.enrichedCore.Expression): Boolean =
            when (e) {
                is za.co.no9.sle.ast.enrichedCore.Unit ->
                    false

                is za.co.no9.sle.ast.enrichedCore.ConstantBool ->
                    false

                is za.co.no9.sle.ast.enrichedCore.ConstantInt ->
                    false

                is za.co.no9.sle.ast.enrichedCore.ConstantString ->
                    false

                is za.co.no9.sle.ast.enrichedCore.ConstantChar ->
                    false

                is za.co.no9.sle.ast.enrichedCore.ConstantRecord ->
                    e.fields.fold(false) { accumulator, field -> accumulator || canFail(field.value) }

                is za.co.no9.sle.ast.enrichedCore.FAIL ->
                    true
                is za.co.no9.sle.ast.enrichedCore.ERROR ->
                    true

                is za.co.no9.sle.ast.enrichedCore.IdReference ->
                    false

                is za.co.no9.sle.ast.enrichedCore.LetExpression ->
                    canFail(e.expression)

                is za.co.no9.sle.ast.enrichedCore.IfExpression ->
                    canFail(e.guardExpression) || canFail(e.thenExpression) || canFail(e.elseExpression)

                is za.co.no9.sle.ast.enrichedCore.LambdaExpression ->
                    canFail(e.expression)

                is za.co.no9.sle.ast.enrichedCore.CallExpression ->
                    canFail(e.operator) || canFail(e.operand)

                is za.co.no9.sle.ast.enrichedCore.FieldProjectionExpression ->
                    canFail(e.record)

                is za.co.no9.sle.ast.enrichedCore.UpdateRecordExpression ->
                    e.updates.fold(canFail(e.record)) { a, b -> a || canFail(b.second) }

                is za.co.no9.sle.ast.enrichedCore.Bar ->
                    e.expressions.fold(false) { a, b -> a || canFail(b) }
            }


    private fun transform(pattern: za.co.no9.sle.ast.enrichedCore.Pattern): ID =
            when (pattern) {
                is za.co.no9.sle.ast.enrichedCore.IdReferencePattern ->
                    ID(pattern.location, pattern.name)

                is za.co.no9.sle.ast.enrichedCore.IgnorePattern ->
                    ID(pattern.location, "_")

                else ->
                    TODO("transform enrichedCore to Core: $pattern")
            }


    private fun transform(constructor: za.co.no9.sle.ast.enrichedCore.Constructor): Constructor =
            Constructor(constructor.location, transform(constructor.name), constructor.arguments)


    private fun transform(id: za.co.no9.sle.ast.enrichedCore.ValueDeclarationID): ValueDeclarationID =
            when (id) {
                is za.co.no9.sle.ast.enrichedCore.LowerIDDeclarationID ->
                    LowerIDDeclarationID(id.location, transform(id.name))

                is za.co.no9.sle.ast.enrichedCore.OperatorDeclarationID ->
                    OperatorDeclarationID(id.location, transform(id.name), id.precedence, id.associativity)
            }


    private fun transform(name: za.co.no9.sle.ast.enrichedCore.ID): ID =
            ID(name.location, name.name)


    private fun <T> List<T>.splitWhile(predicate: (T) -> Boolean): Pair<List<T>, List<T>> {
        val takeList =
                this.takeWhile(predicate)

        val restList =
                this.drop(takeList.size)

        return Pair(takeList, restList)
    }
}


private typealias Constructors =
        Map<String, List<String>>


private fun constructors(environment: Environment): Constructors {
    fun applyBinding(r: Constructors, binding: TypeBinding): Constructors =
            when (binding) {
                is ADTBinding -> {
                    val constructorNames =
                            binding.constructors.map { it.first }

                    constructorNames.fold(r) { a, constructorName ->
                        a + Pair(constructorName, constructorNames)
                    }
                }

                is ImportADTBinding -> {
                    val constructorNames =
                            binding.constructors.map { binding.item.resolveConstructor(it.first) }

                    constructorNames.fold(r) { a, constructorName ->
                        a + Pair(constructorName, constructorNames)
                    }
                }

                else ->
                    r
            }


    fun applyBindings(r: Constructors, environment: Environment): Constructors =
            environment.typeBindings.values.fold(r, ::applyBinding)


    return environment.valueBindings.values.fold(applyBindings(emptyMap(), environment)) { r, valueBinding ->
        if (valueBinding is ImportBinding)
            applyBindings(r, valueBinding.environment)
        else
            r
    }
}