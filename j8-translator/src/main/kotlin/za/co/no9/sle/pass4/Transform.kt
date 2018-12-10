package za.co.no9.sle.pass4

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.comments.JavadocComment
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.*
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.type.PrimitiveType
import za.co.no9.sle.ast.core.*
import za.co.no9.sle.ast.core.Expression
import za.co.no9.sle.ast.core.Unit
import za.co.no9.sle.typing.TArr
import za.co.no9.sle.typing.Type
import za.co.no9.sle.typing.generalise


private val RefMapping = mapOf(
        Pair("i_BuiltinValue", "BUILTIN_VALUE"),
        Pair("(&&)", "AMPERSAND_AMPERSAND"),
        Pair("(||)", "BAR_BAR"),
        Pair("(==)", "EQUAL_EQUAL"),
        Pair("(!=)", "BANG_EQUAL"),
        Pair("(<)", "LESS"),
        Pair("(<=)", "LESS_EQUAL"),
        Pair("(>)", "GREATER"),
        Pair("(>=)", "GREATER_EQUAL"),
        Pair("(+)", "PLUS"),
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
                classDeclaration.addOrphanComment(JavadocComment("${declaration.name.name}: ${declaration.scheme.normalize()}"))

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

        classDeclaration.addOrphanComment(JavadocComment("${constructor.name.name}: ${generalise(constructorType).normalize()}"))

        classDeclaration.addFieldWithInitializer(
                javaType(constructorType),
                constructor.name.name,
                constructorExpression(declaration, constructor),
                Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
    }
}


private fun constructorExpression(declaration: TypeDeclaration, constructor: Constructor): com.github.javaparser.ast.expr.Expression {
    data class ExpressionState(val argumentIndex: Int, val type: Type, val expression: com.github.javaparser.ast.expr.Expression)

    val initialExpressionStateInitializer = ArrayInitializerExpr(
            constructor.arguments.foldIndexed(
                    NodeList.nodeList<com.github.javaparser.ast.expr.Expression>(NameExpr("${constructor.name.name}$"))) { index, nodeList, _ ->
                nodeList.addLast(NameExpr("v$index"))
            }
    )

    val initialExpressionState =
            ExpressionState(constructor.arguments.size, declaration.scheme.type,
                    ArrayCreationExpr()
                            .setElementType("Object[]")
                            .setInitializer(initialExpressionStateInitializer))


    val finalExpressionState = constructor.arguments.foldRight(
            initialExpressionState) { type, expressionState ->
        val argumentType =
                TArr(type, expressionState.type)

        val objectTypes =
                javaPairType(argumentType)

        val nextArgumentIndex =
                expressionState.argumentIndex - 1

        val applyMethod =
                MethodDeclaration()
                        .setName("apply")
                        .setType(objectTypes.second)
                        .setModifier(Modifier.PUBLIC, true)
                        .setParameters(NodeList.nodeList(Parameter().setName("v$nextArgumentIndex").setType(objectTypes.first)))
                        .setBody(BlockStmt(NodeList.nodeList(ReturnStmt().setExpression(expressionState.expression))))

        ExpressionState(
                nextArgumentIndex,
                argumentType,
                ObjectCreationExpr()
                        .setType(ClassOrInterfaceType().setName("Function").setTypeArguments(ClassOrInterfaceType().setName(objectTypes.first), ClassOrInterfaceType().setName(objectTypes.second)))
                        .setAnonymousClassBody(NodeList.nodeList(applyMethod))
        )
    }

    return finalExpressionState.expression
}


fun javaType(type: Type): String =
        when (type) {
            is TArr ->
                "Function<${javaType(type.domain)}, ${javaType(type.range)}>"

            else ->
                "Object"
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

            is ERROR ->
                TODO()

            is FAIL ->
                TODO()

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

            is CaseExpression -> {
                val selectorName =
                        expression.variable

                val fullSelectorName =
                        "\$$selectorName"

                val entries =
                        NodeList(expression.clauses.map {
                            SwitchEntryStmt(NameExpr("${it.constructorName}$"),
                                    NodeList(
                                            if (it.variables.isEmpty())
                                                ReturnStmt().setExpression(javaExpression(it.expression)) as Statement
                                            else
                                                BlockStmt(NodeList(ExpressionStmt(VariableDeclarationExpr().setVariables(NodeList(it.variables.mapIndexed { a, b ->
                                                    VariableDeclarator(com.github.javaparser.ast.type.ClassOrInterfaceType().setName("java.lang.Object"), b, ArrayAccessExpr(NameExpr(fullSelectorName), IntegerLiteralExpr(a + 1)))
                                                }))))).addStatement(ReturnStmt().setExpression(javaExpression(it.expression))) as Statement
                                    )
                            )
                        }  + listOf(
                                SwitchEntryStmt().addStatement("throw new RuntimeException(\"No case expression: \" + $fullSelectorName);")
                        ))

                val getMethod =
                        MethodDeclaration()
                                .setName("get")
                                .setType("java.lang.Object")
                                .setModifier(Modifier.PUBLIC, true)
                                .setParameters(NodeList.nodeList())
                                .setBody(BlockStmt(NodeList(
                                        ExpressionStmt(VariableDeclarationExpr()
                                                .setVariables(NodeList(VariableDeclarator(com.github.javaparser.ast.type.ClassOrInterfaceType().setName("java.lang.Object[]"), fullSelectorName, CastExpr(com.github.javaparser.ast.type.ClassOrInterfaceType().setName("java.lang.Object[]"), NameExpr(selectorName)))))),
                                        SwitchStmt()
                                                .setSelector(
                                                        CastExpr(
                                                                PrimitiveType.intType(),
                                                                ArrayAccessExpr(
                                                                        NameExpr(fullSelectorName),
                                                                        IntegerLiteralExpr(0))))
                                                .setEntries(
                                                        entries
                                                ))))

                MethodCallExpr(ObjectCreationExpr()
                        .setType(ClassOrInterfaceType().setName("java.util.function.Supplier").setTypeArguments(com.github.javaparser.ast.type.ClassOrInterfaceType().setName("java.lang.Object")))
                        .setAnonymousClassBody(NodeList.nodeList(getMethod)),
                        "get",
                        NodeList())
            }
        }

