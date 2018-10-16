package za.co.no9.sle.transform.typedPatternToEnrichedCore

import za.co.no9.sle.Either
import za.co.no9.sle.Errors
import za.co.no9.sle.ast.enrichedCore.*
import za.co.no9.sle.ast.enrichedCore.Unit
import za.co.no9.sle.map
import za.co.no9.sle.transform.typelessPatternToTypedPattern.Constraints
import za.co.no9.sle.typing.Environment
import za.co.no9.sle.typing.Substitution
import za.co.no9.sle.typing.TArr
import za.co.no9.sle.typing.Type


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
                    return CallExpression(caseItem.location, expression.type,
                            LambdaExpression(caseItem.location, caseItemType, transform(caseItem.pattern), transform(caseItem.expression)),
                            IdReference(expression.location, operator.type, "\$case"))
                }

                CallExpression(expression.location, expression.type,
                        LambdaExpression(expression.location, TArr(operator.type, expression.type), IdReferencePattern(expression.location, expression.type, "\$case"),
                                expression.items.drop(1).fold(transform(expression.items[0])) { a, b -> Bar(a.location + b.location, caseItemType, a, transform(b)) }),
                        operator
                )
            }
        }


private fun transform(pattern: za.co.no9.sle.ast.typedPattern.Pattern): Pattern =
        when (pattern) {
            is za.co.no9.sle.ast.typedPattern.ConstantIntPattern ->
                ConstantIntPattern(pattern.location, pattern.type, pattern.value)

            is za.co.no9.sle.ast.typedPattern.ConstantBoolPattern ->
                ConstantBoolPattern(pattern.location, pattern.type, pattern.value)

            is za.co.no9.sle.ast.typedPattern.ConstantStringPattern ->
                ConstantStringPattern(pattern.location, pattern.type, pattern.value)

            is za.co.no9.sle.ast.typedPattern.ConstantUnitPattern ->
                ConstantUnitPattern(pattern.location, pattern.type)

            is za.co.no9.sle.ast.typedPattern.IdReferencePattern ->
                IdReferencePattern(pattern.location, pattern.type, pattern.name)

            is za.co.no9.sle.ast.typedPattern.ConstructorReferencePattern ->
                ConstructorReferencePattern(pattern.location, pattern.type, pattern.name, pattern.parameters.map { transform(it) })
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


