package za.co.no9.sle.pass4

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


fun translate(module: Module, packageDeclaration: String, className: String): za.co.no9.sle.pass4.CompilationUnit {
    return za.co.no9.sle.pass4.CompilationUnit(
            packageDeclaration,
            listOf(ImportDeclaration(false, "java.util.function.Function", false),
                    ImportDeclaration(true, "za.co.no9.sle.runtime.Builtin", true)),
            listOf(ClassDeclaration(className, translateTDeclarations(module.declarations) + translateLDeclarations(module.declarations))))
}


private fun translateLDeclarations(declarations: List<za.co.no9.sle.ast.core.Declaration>): List<Declaration> =
        declarations.fold(emptyList()) { a, declaration ->
            when (declaration) {
                is LetDeclaration ->
                    a + MemberDeclaration("${declaration.name.name}: ${declaration.scheme.normalize()}", true, true, true, declaration.name.name, "Object", translate(declaration.expression))

                else ->
                    a
            }
        }


private fun translateTDeclarations(declarations: List<za.co.no9.sle.ast.core.Declaration>): List<Declaration> =
        declarations.fold(emptyList()) { a, b ->
            when (b) {
                is TypeDeclaration ->
                    a + translateTDeclaration(b)
                else ->
                    a
            }

        }

//private fun translateTypeDeclarations(module: Module, classDeclaration: ClassOrInterfaceDeclaration) {
//    for (declaration in module.declarations) {
//        when (declaration) {
//            is TypeDeclaration ->
//                translateTypeDeclaration(declaration, classDeclaration)
//        }
//    }
//}


private fun translateTDeclaration(declaration: TypeDeclaration): List<Declaration> =
        declaration.constructors.withIndex().fold(emptyList()) { a, b ->
            val constructorType =
                    b.value.arguments.foldRight(declaration.scheme.type) { a, b -> TArr(a, b) }

            a +
                    MemberDeclaration(null, true, true, true,
                            "${b.value.name.name}$",
                            "int",
                            IntegerLiteralExpression(b.index)) +
                    MemberDeclaration("${b.value.name.name}: ${generalise(constructorType).normalize()}", true, true, true,
                            b.value.name.name,
                            "java.lang.Object",
                            constructorE(declaration, b.value))
        }


private fun constructorE(declaration: TypeDeclaration, constructor: Constructor): za.co.no9.sle.pass4.Expression {
    data class ExpressionState(val argumentIndex: Int, val type: Type, val expression: za.co.no9.sle.pass4.Expression)

    val initialExpressionStateInitializer =
            ArrayInitialisationExpression(
                    "java.lang.Object[]",
                    constructor.arguments.foldIndexed(
                            listOf(NameExpression("${constructor.name.name}$"))) { index, nodeList, _ ->
                        nodeList + NameExpression("v$index")
                    }
            )

    val initialExpressionState =
            ExpressionState(constructor.arguments.size, declaration.scheme.type, initialExpressionStateInitializer)


    val finalExpressionState = constructor.arguments.foldRight(
            initialExpressionState) { type, expressionState ->
        val argumentType =
                TArr(type, expressionState.type)

//        val objectTypes =
//                javaPairType(argumentType)

        val nextArgumentIndex =
                expressionState.argumentIndex - 1

//        val applyMethod =
//                MethodDeclaration()
//                        .setName("apply")
//                        .setType(objectTypes.second)
//                        .setModifier(Modifier.PUBLIC, true)
//                        .setParameters(NodeList.nodeList(Parameter().setName("v$nextArgumentIndex").setType(objectTypes.first)))
//                        .setBody(BlockStmt(NodeList.nodeList(ReturnStmt().setExpression(expressionState.expression))))

        ExpressionState(
                nextArgumentIndex,
                argumentType,
                AnonymousObjectCreationExpression(AnonymousClassDeclaration("Function<java.lang.Object, java.lang.Object>", listOf(
                        za.co.no9.sle.pass4.MethodDeclaration(
                                true, false, false,
                                "apply",
                                "java.lang.Object",
                                listOf(za.co.no9.sle.pass4.Parameter("v$nextArgumentIndex", "java.lang.Object")),
                                StatementBlock(listOf(
                                        ReturnStatement(expressionState.expression)
                                ))
                        )
                ))
                )
//                ObjectCreationExpr()
//                        .setType(ClassOrInterfaceType().setName("Function").setTypeArguments(ClassOrInterfaceType().setName(objectTypes.first), ClassOrInterfaceType().setName(objectTypes.second)))
//                        .setAnonymousClassBody(NodeList.nodeList(applyMethod))
        )
    }

    return finalExpressionState.expression
}


//private fun translateTypeDeclaration(declaration: TypeDeclaration, classDeclaration: ClassOrInterfaceDeclaration) {
//    for ((constructorIndex, constructor) in declaration.constructors.withIndex()) {
//        classDeclaration.addFieldWithInitializer(
//                "int",
//                "${constructor.name.name}$",
//                IntegerLiteralExpr(constructorIndex),
//                Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
//
//        val constructorType =
//                constructor.arguments.foldRight(declaration.scheme.type) { a, b -> TArr(a, b) }
//
//        classDeclaration.addOrphanComment(JavadocComment("${constructor.name.name}: ${generalise(constructorType).normalize()}"))
//
//        classDeclaration.addFieldWithInitializer(
//                javaType(constructorType),
//                constructor.name.name,
//                constructorExpression(declaration, constructor),
//                Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
//    }
//}


//private fun constructorExpression(declaration: TypeDeclaration, constructor: Constructor): com.github.javaparser.ast.expr.Expression {
//    data class ExpressionState(val argumentIndex: Int, val type: Type, val expression: com.github.javaparser.ast.expr.Expression)
//
//    val initialExpressionStateInitializer = ArrayInitializerExpr(
//            constructor.arguments.foldIndexed(
//                    NodeList.nodeList<com.github.javaparser.ast.expr.Expression>(NameExpr("${constructor.name.name}$"))) { index, nodeList, _ ->
//                nodeList.addLast(NameExpr("v$index"))
//            }
//    )
//
//    val initialExpressionState =
//            ExpressionState(constructor.arguments.size, declaration.scheme.type,
//                    ArrayCreationExpr()
//                            .setElementType("Object[]")
//                            .setInitializer(initialExpressionStateInitializer))
//
//
//    val finalExpressionState = constructor.arguments.foldRight(
//            initialExpressionState) { type, expressionState ->
//        val argumentType =
//                TArr(type, expressionState.type)
//
//        val objectTypes =
//                javaPairType(argumentType)
//
//        val nextArgumentIndex =
//                expressionState.argumentIndex - 1
//
//        val applyMethod =
//                MethodDeclaration()
//                        .setName("apply")
//                        .setType(objectTypes.second)
//                        .setModifier(Modifier.PUBLIC, true)
//                        .setParameters(NodeList.nodeList(Parameter().setName("v$nextArgumentIndex").setType(objectTypes.first)))
//                        .setBody(BlockStmt(NodeList.nodeList(ReturnStmt().setExpression(expressionState.expression))))
//
//        ExpressionState(
//                nextArgumentIndex,
//                argumentType,
//                ObjectCreationExpr()
//                        .setType(ClassOrInterfaceType().setName("Function").setTypeArguments(ClassOrInterfaceType().setName(objectTypes.first), ClassOrInterfaceType().setName(objectTypes.second)))
//                        .setAnonymousClassBody(NodeList.nodeList(applyMethod))
//        )
//    }
//
//    return finalExpressionState.expression
//}


fun javaType(type: Type): String =
        when (type) {
            is TArr ->
                "Function<${javaType(type.domain)}, ${javaType(type.range)}>"

            else ->
                "Object"
        }


//private fun javaPairType(type: Type): Pair<String, String> =
//        when (type) {
//            is TArr ->
//                Pair(javaType(type.domain), javaType(type.range))
//
//            else ->
//                Pair(javaType(type), javaType(type))
//        }


private fun translate(expression: Expression): za.co.no9.sle.pass4.Expression =
        when (expression) {
            is Unit ->
                NameExpression("za.co.no9.sle.runtime.Unit.INSTANCE")

            is ConstantBool ->
                BoolLiteralExpression(expression.value)

            is ConstantInt ->
                IntegerLiteralExpression(expression.value)

            is ConstantString ->
                StringLiteralExpression(expression.value)

            is ERROR ->
                TODO()

            is FAIL ->
                TODO()

            is IdReference -> {
                val refMappingName =
                        RefMapping[expression.name]

                when (refMappingName) {
                    null ->
                        NameExpression(expression.name)

                    else ->
                        NameExpression(refMappingName)
                }
            }

            is IfExpression ->
                ConditionalExpression(TypeCastExpression("java.lang.Boolean", translate(expression.guardExpression)), translate(expression.thenExpression), translate(expression.elseExpression))

            is LambdaExpression ->
                AnonymousObjectCreationExpression(
                        AnonymousClassDeclaration("Function<Object, Object>", listOf(
                                za.co.no9.sle.pass4.MethodDeclaration(
                                        true,
                                        false,
                                        false,
                                        "apply",
                                        "Object",
                                        listOf(Parameter(expression.argument.name, "Object")),
                                        StatementBlock(listOf(
                                                ReturnStatement(translate(expression.expression))
                                        ))
                                )
                        ))
                )

            is CallExpression ->
                MethodCallExpression(
                        TypeCastExpression("Function<Object, Object>", translate(expression.operator)),
                        "apply",
                        listOf(translate(expression.operand)))

            is CaseExpression -> {
                val selectorName =
                        expression.variable

                val fullSelectorName =
                        "\$$selectorName"

                val entries =
                        expression.clauses.map {
                            SwitchStatementEntry(
                                    NameExpression("${it.constructorName}$"),
                                    if (it.variables.isEmpty())
                                        ReturnStatement(translate(it.expression))
                                    else
                                        BlockStatement(
                                                it.variables.mapIndexed { index, s ->
                                                    VariableDeclarationStatement("java.lang.Object", s, ArrayAccessExpression(NameExpression(fullSelectorName), IntegerLiteralExpression(index + 1)))
                                                } + ReturnStatement(translate(it.expression))
                                        )
                            )
                        }

                MethodCallExpression(
                        AnonymousObjectCreationExpression(
                                AnonymousClassDeclaration(
                                        "java.util.function.Supplier<java.lang.Object>",
                                        listOf(za.co.no9.sle.pass4.MethodDeclaration(
                                                true, false, false,
                                                "get",
                                                "java.lang.Object",
                                                emptyList(),
                                                StatementBlock(listOf(
                                                        VariableDeclarationStatement("java.lang.Object[]", fullSelectorName, TypeCastExpression("java.lang.Object[]", NameExpression(selectorName))),
                                                        SwitchStatement(
                                                                TypeCastExpression("int", ArrayAccessExpression(NameExpression(fullSelectorName), IntegerLiteralExpression(0))),
                                                                entries,
                                                                ThrowStatement(ObjectCreationExpression("java.lang.RuntimeException", listOf(OpExpression(StringLiteralExpression("No case expression: "), "+", NameExpression(fullSelectorName)))))
                                                        )
                                                ))
                                        )))
                        ),
                        "get",
                        emptyList())
            }

            else ->
                IntegerLiteralExpression(-1)
        }
