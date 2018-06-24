package za.co.no9.sle.ast.pass2


fun map(ast: za.co.no9.sle.ast.pass1.Module): Module =
        Module(ast.location, ast.declarations.map { map(it) })


fun map(ast: za.co.no9.sle.ast.pass1.Declaration): Declaration =
        when (ast) {
            is za.co.no9.sle.ast.pass1.LetDeclaration ->
                LetDeclaration(ast.location, map(ast.name), ast.arguments.foldRight(map(ast.expression)) { name, expression -> LambdaExpression(ast.location, map(name), expression) })
        }


fun map(ast: za.co.no9.sle.ast.pass1.ID): ID =
        ID(ast.location, ast.name)


fun map(ast: za.co.no9.sle.ast.pass1.Expression): Expression =
        when (ast) {
            is za.co.no9.sle.ast.pass1.True ->
                ConstantBool(ast.location, true)

            is za.co.no9.sle.ast.pass1.False ->
                ConstantBool(ast.location, false)

            is za.co.no9.sle.ast.pass1.ConstantInt ->
                ConstantInt(ast.location, ast.value)

            is za.co.no9.sle.ast.pass1.ConstantString ->
                ConstantString(ast.location, ast.value)

            is za.co.no9.sle.ast.pass1.NotExpression ->
                CallExpression(ast.location, IdReference(ast.location, "(!)"), map(ast.expression))

            is za.co.no9.sle.ast.pass1.IdReference ->
                IdReference(ast.location, ast.name)

            is za.co.no9.sle.ast.pass1.IfExpression ->
                IfExpression(ast.location, map(ast.guardExpression), map(ast.thenExpression), map(ast.elseExpression))

            is za.co.no9.sle.ast.pass1.LambdaExpression ->
                ast.arguments.foldRight(map(ast.expression)) { name, expression -> LambdaExpression(ast.location, map(name), expression) }

            is za.co.no9.sle.ast.pass1.BinaryOpExpression ->
                CallExpression(ast.location, CallExpression(ast.operator.location, IdReference(ast.operator.location, "(${ast.operator.name})"), map(ast.left)), map(ast.right))

            is za.co.no9.sle.ast.pass1.CallExpression ->
                ast.operands.fold(map(ast.operator)) { expression, operand -> CallExpression(ast.location, expression, map(operand)) }
        }