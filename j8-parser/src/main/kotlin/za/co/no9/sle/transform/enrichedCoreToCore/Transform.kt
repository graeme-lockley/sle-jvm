package za.co.no9.sle.transform.enrichedCoreToCore

import za.co.no9.sle.Either
import za.co.no9.sle.Errors
import za.co.no9.sle.ast.core.*
import za.co.no9.sle.ast.core.Unit
import za.co.no9.sle.map
import za.co.no9.sle.transform.typelessPatternToTypedPattern.Constraints
import za.co.no9.sle.typing.Environment
import za.co.no9.sle.typing.Substitution


data class Detail(
        val constraints: Constraints,
        val substitution: Substitution,
        val unresolvedModule: za.co.no9.sle.ast.typedPattern.Module,
        val resolvedModule: za.co.no9.sle.ast.typedPattern.Module,
        val enrichedCore: za.co.no9.sle.ast.enrichedCore.Module,
        val coreModule: Module)


fun parseWithDetail(text: String, environment: Environment): Either<Errors, Detail> {
    val typePatternDetail =
            za.co.no9.sle.transform.typedPatternToEnrichedCore.parseWithDetail(text, environment)

    return typePatternDetail.map {
        Detail(it.constraints, it.substitution, it.unresolvedModule, it.resolvedModule, it.enrichedModule, transform(it.enrichedModule))
    }
}


fun parse(text: String, environment: Environment): Either<Errors, Module> {
    return za.co.no9.sle.transform.typedPatternToEnrichedCore.parse(text, environment).map { transform(it) }
}


private fun transform(module: za.co.no9.sle.ast.enrichedCore.Module): Module =
        Module(module.location, module.declarations.map { transform(it) })


private fun transform(declaration: za.co.no9.sle.ast.enrichedCore.Declaration): Declaration =
        when (declaration) {
            is za.co.no9.sle.ast.enrichedCore.LetDeclaration ->
                LetDeclaration(declaration.location, declaration.scheme, transform(declaration.name), transform(declaration.expression))

            is za.co.no9.sle.ast.enrichedCore.TypeAliasDeclaration ->
                TypeAliasDeclaration(declaration.location, transform(declaration.name), declaration.scheme)

            is za.co.no9.sle.ast.enrichedCore.TypeDeclaration ->
                TypeDeclaration(declaration.location, transform(declaration.name), declaration.scheme, declaration.constructors.map { transform(it) })
        }


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

            is za.co.no9.sle.ast.enrichedCore.FAIL ->
                TODO("FAIL")

            is za.co.no9.sle.ast.enrichedCore.ERROR ->
                TODO("ERROR")

            is za.co.no9.sle.ast.enrichedCore.IdReference ->
                IdReference(expression.location, expression.type, expression.name)

            is za.co.no9.sle.ast.enrichedCore.IfExpression ->
                IfExpression(expression.location, expression.type, transform(expression.guardExpression), transform(expression.thenExpression), transform(expression.elseExpression))

            is za.co.no9.sle.ast.enrichedCore.LambdaExpression ->
                LambdaExpression(expression.location, expression.type, transform(expression.argument), transform(expression.expression))

            is za.co.no9.sle.ast.enrichedCore.CallExpression ->
                CallExpression(expression.location, expression.type, transform(expression.operator), transform(expression.operand))

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

                        for (i in 1 .. n) {
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

                Match().match(names, expression.expressions.filter { !isError(it) }.map { createQ(it) }, expression.expressions.filter { isError(it) }[0])
            }
        }


typealias Patterns =
        List<za.co.no9.sle.ast.enrichedCore.Pattern>

typealias Q =
        Pair<Patterns, za.co.no9.sle.ast.enrichedCore.Expression>

typealias Qs =
        List<Q>


private fun isError(e: za.co.no9.sle.ast.enrichedCore.Expression): Boolean =
        e is za.co.no9.sle.ast.enrichedCore.ERROR


class Match(private var counter: Int = 0) {
    fun variable(): String {
        val variable =
                "$$$counter"

        counter += 1

        return variable
    }


    fun variables(n: Int): List<String> {
        val result =
                mutableListOf<String>()

        for (i in 1..n)
            result.add(variable())

        return result
    }


    fun match(us: List<String>, qs: Qs, e: za.co.no9.sle.ast.enrichedCore.Expression): Expression =
            if (us.isEmpty()) {
                transform(combineExpressionWithFail(qs.map { it.second }, e))
            } else {
                val firstFirstPattern =
                        qs[0].first[0]

                if (firstFirstPattern is za.co.no9.sle.ast.enrichedCore.ConstructorReferencePattern) {
                    CaseExpression(e.location, e.type, us[0], qs.groupBy { (it.first[0] as za.co.no9.sle.ast.enrichedCore.ConstructorReferencePattern).name }.map {
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
                        }, e))
                    })
                } else {
                    val variableName =
                            (firstFirstPattern as za.co.no9.sle.ast.enrichedCore.IdReferencePattern).name

                    match(us.drop(1), qs.map {
                        Pair(it.first.drop(1), substitute(it.second, variableName, us[0]))
                    }, e)
                }
            }
}


private fun combineExpressionWithFail(expressions: List<za.co.no9.sle.ast.enrichedCore.Expression>, e: za.co.no9.sle.ast.enrichedCore.Expression): za.co.no9.sle.ast.enrichedCore.Expression =
        when {
            expressions.isEmpty() ->
                e

            canFail(expressions[0]) ->
                replaceFailWith(expressions[0], combineExpressionWithFail(expressions.drop(1), e))

            else ->
                expressions[0]
        }


private fun replaceFailWith(haystack: za.co.no9.sle.ast.enrichedCore.Expression, needle: za.co.no9.sle.ast.enrichedCore.Expression): za.co.no9.sle.ast.enrichedCore.Expression =
        when (haystack) {
            is za.co.no9.sle.ast.enrichedCore.Unit ->
                haystack

            is za.co.no9.sle.ast.enrichedCore.ConstantBool ->
                haystack

            is za.co.no9.sle.ast.enrichedCore.ConstantInt ->
                haystack

            is za.co.no9.sle.ast.enrichedCore.ConstantString ->
                haystack

            is za.co.no9.sle.ast.enrichedCore.FAIL ->
                needle

            is za.co.no9.sle.ast.enrichedCore.ERROR ->
                haystack

            is za.co.no9.sle.ast.enrichedCore.IdReference ->
                haystack

            is za.co.no9.sle.ast.enrichedCore.IfExpression ->
                za.co.no9.sle.ast.enrichedCore.IfExpression(haystack.location, haystack.type, replaceFailWith(haystack.guardExpression, needle), replaceFailWith(haystack.thenExpression, needle), replaceFailWith(haystack.elseExpression, needle))

            is za.co.no9.sle.ast.enrichedCore.LambdaExpression ->
                za.co.no9.sle.ast.enrichedCore.LambdaExpression(haystack.location, haystack.type, haystack.argument, replaceFailWith(haystack.expression, needle))

            is za.co.no9.sle.ast.enrichedCore.CallExpression ->
                za.co.no9.sle.ast.enrichedCore.CallExpression(haystack.location, haystack.type, replaceFailWith(haystack.operator, needle), replaceFailWith(haystack.operand, needle))

            is za.co.no9.sle.ast.enrichedCore.Bar ->
                za.co.no9.sle.ast.enrichedCore.Bar(haystack.location, haystack.type, haystack.expressions.map { replaceFailWith(it, needle) })
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

            is za.co.no9.sle.ast.enrichedCore.FAIL ->
                e

            is za.co.no9.sle.ast.enrichedCore.ERROR ->
                e

            is za.co.no9.sle.ast.enrichedCore.IdReference ->
                if (e.name == old)
                    za.co.no9.sle.ast.enrichedCore.IdReference(e.location, e.type, new)
                else
                    e

            is za.co.no9.sle.ast.enrichedCore.IfExpression ->
                za.co.no9.sle.ast.enrichedCore.IfExpression(e.location, e.type, substitute(e.guardExpression, old, new), substitute(e.thenExpression, old, new), substitute(e.elseExpression, old, new))

            is za.co.no9.sle.ast.enrichedCore.LambdaExpression ->
                za.co.no9.sle.ast.enrichedCore.LambdaExpression(e.location, e.type, e.argument, substitute(e.expression, old, new))

            is za.co.no9.sle.ast.enrichedCore.CallExpression ->
                za.co.no9.sle.ast.enrichedCore.CallExpression(e.location, e.type, substitute(e.operator, old, new), substitute(e.operand, old, new))

            is za.co.no9.sle.ast.enrichedCore.Bar ->
                za.co.no9.sle.ast.enrichedCore.Bar(e.location, e.type, e.expressions.map { substitute(it, old, new) })
        }


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

            is za.co.no9.sle.ast.enrichedCore.FAIL ->
                true
            is za.co.no9.sle.ast.enrichedCore.ERROR ->
                true

            is za.co.no9.sle.ast.enrichedCore.IdReference ->
                false

            is za.co.no9.sle.ast.enrichedCore.IfExpression ->
                canFail(e.guardExpression) || canFail(e.thenExpression) || canFail(e.elseExpression)

            is za.co.no9.sle.ast.enrichedCore.LambdaExpression ->
                canFail(e.expression)

            is za.co.no9.sle.ast.enrichedCore.CallExpression ->
                canFail(e.operator) || canFail(e.operand)

            is za.co.no9.sle.ast.enrichedCore.Bar ->
                e.expressions.fold(false) { a, b -> a || canFail(b) }
        }


private fun transform(pattern: za.co.no9.sle.ast.enrichedCore.Pattern): ID =
        when (pattern) {
            is za.co.no9.sle.ast.enrichedCore.IdReferencePattern ->
                ID(pattern.location, pattern.name)

            else ->
                TODO("transform enrichedCore to Core: $pattern")
        }


private fun transform(constructor: za.co.no9.sle.ast.enrichedCore.Constructor): Constructor =
        Constructor(constructor.location, transform(constructor.name), constructor.arguments)


private fun transform(name: za.co.no9.sle.ast.enrichedCore.ID): ID =
        ID(name.location, name.name)
