package za.co.no9.sle.pass4

import za.co.no9.sle.ast.core.CallExpression
import za.co.no9.sle.ast.core.Module


fun optimize(module: Module): Module =
        Module(module.location, module.exports, optimize(module.declarations))


private fun optimize(declarations: List<za.co.no9.sle.ast.core.Declaration>): List<za.co.no9.sle.ast.core.Declaration> =
        declarations.map { optimize(it) }


private fun optimize(declaration: za.co.no9.sle.ast.core.Declaration): za.co.no9.sle.ast.core.Declaration =
        when (declaration) {
            is za.co.no9.sle.ast.core.LetDeclaration ->
                optimize(declaration)

            else ->
                declaration
        }


private fun optimize(declaration: za.co.no9.sle.ast.core.LetDeclaration): za.co.no9.sle.ast.core.LetDeclaration =
        za.co.no9.sle.ast.core.LetDeclaration(declaration.location, declaration.scheme, declaration.name, optimize(declaration.expression))


private fun optimize(expression: za.co.no9.sle.ast.core.Expression): za.co.no9.sle.ast.core.Expression =
        when (expression) {
            is CallExpression -> {
                val operator =
                        expression.operator

                val operand =
                        expression.operand

                if (operator is za.co.no9.sle.ast.core.IdReference && operator.name == "i_BuiltinValue" && operand is za.co.no9.sle.ast.core.ConstantString)
                    za.co.no9.sle.ast.core.IdReference(operand.location, expression.type, operand.value)
                else
                    expression
            }

            else ->
                expression
        }

