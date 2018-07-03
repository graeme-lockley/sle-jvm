package za.co.no9.sle.pass2

import za.co.no9.sle.pass1.BinaryOpExpression
import za.co.no9.sle.pass1.False
import za.co.no9.sle.pass1.NotExpression
import za.co.no9.sle.pass1.True


fun astToCoreAST(ast: za.co.no9.sle.pass1.Module): Module =
        Module(ast.location, ast.declarations.map { astToCoreAST(it) })


fun astToCoreAST(ast: za.co.no9.sle.pass1.Declaration): Declaration =
        when (ast) {
            is za.co.no9.sle.pass1.LetDeclaration ->
                LetDeclaration(ast.location, astToCoreAST(ast.name), ast.arguments.foldRight(astToCoreAST(ast.expression)) { name, expression -> LambdaExpression(ast.location, astToCoreAST(name), expression) })
        }


fun astToCoreAST(ast: za.co.no9.sle.pass1.ID): ID =
        ID(ast.location, ast.name)


fun astToCoreAST(ast: za.co.no9.sle.pass1.Expression): Expression =
        when (ast) {
            is True ->
                ConstantBool(ast.location, true)

            is False ->
                ConstantBool(ast.location, false)

            is za.co.no9.sle.pass1.ConstantInt ->
                ConstantInt(ast.location, ast.value)

            is za.co.no9.sle.pass1.ConstantString ->
                ConstantString(ast.location, ast.value)

            is NotExpression ->
                CallExpression(ast.location, IdReference(ast.location, "(!)"), astToCoreAST(ast.expression))

            is za.co.no9.sle.pass1.IdReference ->
                IdReference(ast.location, ast.name)

            is za.co.no9.sle.pass1.IfExpression ->
                IfExpression(ast.location, astToCoreAST(ast.guardExpression), astToCoreAST(ast.thenExpression), astToCoreAST(ast.elseExpression))

            is za.co.no9.sle.pass1.LambdaExpression ->
                ast.arguments.foldRight(astToCoreAST(ast.expression)) { name, expression -> LambdaExpression(ast.location, astToCoreAST(name), expression) }

            is BinaryOpExpression ->
                CallExpression(ast.location, CallExpression(ast.operator.location, IdReference(ast.operator.location, "(${ast.operator.name})"), astToCoreAST(ast.left)), astToCoreAST(ast.right))

            is za.co.no9.sle.pass1.CallExpression ->
                ast.operands.fold(astToCoreAST(ast.operator)) { expression, operand -> CallExpression(ast.location, expression, astToCoreAST(operand)) }
        }