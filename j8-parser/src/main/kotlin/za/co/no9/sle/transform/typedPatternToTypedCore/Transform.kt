package za.co.no9.sle.transform.typedPatternToTypedCore

import za.co.no9.sle.Either
import za.co.no9.sle.Errors
import za.co.no9.sle.ast.typedCore.*
import za.co.no9.sle.ast.typedCore.Unit
import za.co.no9.sle.map
import za.co.no9.sle.transform.typelessPatternToTypedPattern.Constraints
import za.co.no9.sle.typing.Environment
import za.co.no9.sle.typing.Substitution


data class Detail(
        val constraints: Constraints,
        val substitution: Substitution,
        val unresolvedModule: za.co.no9.sle.ast.typedPattern.Module,
        val resolvedModule: za.co.no9.sle.ast.typedPattern.Module,
        val coreModule: Module)


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
                LambdaExpression(expression.location, expression.type, transform(expression.argument), transform(expression.expression))

            is za.co.no9.sle.ast.typedPattern.CallExpression ->
                CallExpression(expression.location, expression.type, transform(expression.operator), transform(expression.operand))
        }


private fun transform(name: za.co.no9.sle.ast.typedPattern.ID): ID =
        ID(name.location, name.name)


private fun transform(constructor: za.co.no9.sle.ast.typedPattern.Constructor): Constructor =
        Constructor(constructor.location, transform(constructor.name), constructor.arguments)

