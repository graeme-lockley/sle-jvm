package za.co.no9.sle.pass4

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.comments.JavadocComment
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.stmt.ReturnStmt
import com.github.javaparser.ast.type.ClassOrInterfaceType
import za.co.no9.sle.ast.typedCore.*
import za.co.no9.sle.ast.typedCore.Expression
import za.co.no9.sle.ast.typedCore.Unit
import za.co.no9.sle.typing.*


private val RefMapping = mapOf(
        Pair("(&&)", "AMPERSAND_AMPERSAND"),
        Pair("(||)", "BAR_BAR"),
        Pair("(==)", "EQUAL_EQUAL"),
        Pair("(!=)", "BANG_EQUAL"),
        Pair("(<)", "LESS"),
        Pair("(<=)", "LESS_EQUAL"),
        Pair("(>)", "GREATER"),
        Pair("(>=)", "GREATER_EQUAL"),
        Pair("(-)", "MINUS"),
        Pair("(*)", "STAR"),
        Pair("(/)", "SLASH")
)


fun translateToJava(module: Module, packageDeclaration: String, className: String): CompilationUnit {
    val compilationUnit =
            CompilationUnit()

    if (packageDeclaration.isNotEmpty()) {
        compilationUnit.setPackageDeclaration(packageDeclaration)
    }
    compilationUnit.addImport("java.util.function.Function", false, false)
    compilationUnit.addImport("za.co.no9.sle.runtime.Builtin", true, true)

    val classDeclaration =
            compilationUnit.addClass(className)
                    .setPublic(true)

    translateTypeDeclarations(module, classDeclaration)
    translateLetDeclarations(module, classDeclaration)

    return compilationUnit
}


private fun translateLetDeclarations(module: Module, classDeclaration: ClassOrInterfaceDeclaration) {
    for (declaration in module.declarations) {
        when (declaration) {
            is LetDeclaration -> {
                classDeclaration.addOrphanComment(JavadocComment("${declaration.name.name}: ${declaration.scheme}"))

                classDeclaration.addFieldWithInitializer(
                        javaType(declaration.scheme.type),
                        declaration.name.name,
                        javaExpression(declaration.expression),
                        Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            }
        }
    }
}


private fun translateTypeDeclarations(module: Module, classDeclaration: ClassOrInterfaceDeclaration) {
    for (declaration in module.declarations) {
        when (declaration) {
            is TypeDeclaration ->
                translateTypeDeclaration(declaration, classDeclaration)
        }
    }
}


private fun translateTypeDeclaration(declaration: TypeDeclaration, classDeclaration: ClassOrInterfaceDeclaration) {
    for ((constructorIndex, constructor) in declaration.constructors.withIndex()) {
        classDeclaration.addFieldWithInitializer(
                "int",
                "${constructor.name.name}$",
                IntegerLiteralExpr(constructorIndex),
                Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)

        val constructorType =
                constructor.arguments.foldRight(declaration.scheme.type) { a, b -> TArr(a, b) }

        classDeclaration.addOrphanComment(JavadocComment("${constructor.name.name}: ${generalise(constructorType)}"))

        classDeclaration.addFieldWithInitializer(
                javaType(constructorType),
                constructor.name.name,
                constructorExpression(declaration, constructor),
                Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
    }
}


private fun constructorExpression(declaration: TypeDeclaration, constructor: Constructor): com.github.javaparser.ast.expr.Expression {
    data class ExpressionState(val argumentIndex: Int, val type: Type, val expression: com.github.javaparser.ast.expr.Expression)

    val initialExpression: NodeList<com.github.javaparser.ast.expr.Expression> =
            NodeList.nodeList(NameExpr("${constructor.name.name}$"))

    val acc: (Int, NodeList<com.github.javaparser.ast.expr.Expression>, Type) -> NodeList<com.github.javaparser.ast.expr.Expression> =
            { index: Int, nodeList: NodeList<com.github.javaparser.ast.expr.Expression>, _: Type -> nodeList.addLast(NameExpr("v$index")) }

    val initExpressionState =
            ExpressionState(constructor.arguments.size, declaration.scheme.type,
                    ArrayCreationExpr()
                            .setElementType("Object[]")
                            .setInitializer(ArrayInitializerExpr(constructor.arguments.foldIndexed(initialExpression, acc))))


    val expression = constructor.arguments.foldRight(initExpressionState) { type, expressionState ->
        val argumentType =
                TArr(type, expressionState.type)

        val objectTypes =
                javaPairType(argumentType)

        val applyMethod =
                MethodDeclaration()
                        .setName("apply")
                        .setType(objectTypes.second)
                        .setModifier(Modifier.PUBLIC, true)
                        .setParameters(NodeList.nodeList(Parameter().setName("v${expressionState.argumentIndex - 1}").setType(objectTypes.first)))
                        .setBody(BlockStmt(NodeList.nodeList(ReturnStmt().setExpression(expressionState.expression))))

        ExpressionState(
                expressionState.argumentIndex - 1,
                argumentType,
                ObjectCreationExpr()
                        .setType(ClassOrInterfaceType().setName("Function").setTypeArguments(ClassOrInterfaceType().setName(objectTypes.first), ClassOrInterfaceType().setName(objectTypes.second)))
                        .setAnonymousClassBody(NodeList.nodeList(applyMethod))
        )
    }

    return expression.expression
}


fun javaType(type: Type): String =
        when (type) {
            is TCon ->
                when {
                    type.name == "Int" ->
                        "Integer"

                    type.name == "Bool" ->
                        "Boolean"

                    type.name == "String" ->
                        "String"

                    type.name == "()" ->
                        "za.co.no9.sle.runtime.Unit"

                    else ->
                        "Object"
                }

            is TVar ->
                "Object"

            is TArr ->
                "Function<${javaType(type.domain)}, ${javaType(type.range)}>"
        }


private fun javaPairType(type: Type): Pair<String, String> =
        when (type) {
            is TArr ->
                Pair(javaType(type.domain), javaType(type.range))

            else ->
                Pair(javaType(type), javaType(type))
        }


private fun javaExpression(expression: Expression): com.github.javaparser.ast.expr.Expression =
        when (expression) {
            is Unit ->
                NameExpr("za.co.no9.sle.runtime.Unit.INSTANCE")

            is ConstantBool ->
                BooleanLiteralExpr(expression.value)

            is ConstantInt ->
                IntegerLiteralExpr(expression.value)

            is ConstantString ->
                StringLiteralExpr(expression.value)

            is IdReference -> {
                val refMappingName =
                        RefMapping[expression.name]

                when (refMappingName) {
                    null ->
                        NameExpr(expression.name)

                    else ->
                        NameExpr(refMappingName)
                }
            }

            is IfExpression ->
                ConditionalExpr(javaExpression(expression.guardExpression), javaExpression(expression.thenExpression), javaExpression(expression.elseExpression))

            is LambdaExpression -> {
                val objectTypes =
                        javaPairType(expression.type)

                val applyMethod =
                        MethodDeclaration()
                                .setName("apply")
                                .setType(objectTypes.second)
                                .setModifier(Modifier.PUBLIC, true)
                                .setParameters(NodeList.nodeList(Parameter().setName(expression.argument.name).setType(objectTypes.first)))
                                .setBody(BlockStmt(NodeList.nodeList(ReturnStmt().setExpression(javaExpression(expression.expression)))))

                ObjectCreationExpr()
                        .setType(ClassOrInterfaceType().setName("Function").setTypeArguments(com.github.javaparser.ast.type.ClassOrInterfaceType().setName(objectTypes.first), com.github.javaparser.ast.type.ClassOrInterfaceType().setName(objectTypes.second)))
                        .setAnonymousClassBody(NodeList.nodeList(applyMethod))
            }

            is CallExpression ->
                MethodCallExpr(
                        javaExpression(expression.operator),
                        "apply",
                        NodeList.nodeList(javaExpression(expression.operand)))
        }

