package za.co.no9.sle.pass4

import za.co.no9.sle.ast.core.*
import za.co.no9.sle.ast.core.Expression
import za.co.no9.sle.ast.core.Unit
import za.co.no9.sle.typing.TArr
import za.co.no9.sle.typing.Type
import za.co.no9.sle.typing.generalise


private val RefMapping = mapOf(
        Pair("i_BuiltinValue", "za.co.no9.sle.runtime.Builtin.BUILTIN_VALUE")
)


fun translate(module: Module, packageDeclaration: String, className: String): za.co.no9.sle.pass4.CompilationUnit {
    val optimizedModule =
            optimize(module)

    return za.co.no9.sle.pass4.CompilationUnit(
            packageDeclaration,
            listOf(),
            listOf(ClassDeclaration(className, translateTypeDeclarations(optimizedModule.declarations) + translateLetDeclarations(optimizedModule.declarations))))
}


private fun translateLetDeclarations(declarations: List<za.co.no9.sle.ast.core.Declaration>): List<Declaration> =
        declarations.fold(emptyList()) { a, declaration ->
            when (declaration) {
                is LetDeclaration -> {
                    val declarationCommentName =
                            when (declaration.id) {
                                is LowerIDDeclarationID ->
                                    declaration.id.name.name

                                is OperatorDeclarationID ->
                                    "(${declaration.id.name.name})"
                            }

                    a + MemberDeclaration("$declarationCommentName: ${declaration.scheme.normalize()}", true, true, true, markupName(declaration.id.name.name), "java.lang.Object", translate(declaration.expression))
                }

                else ->
                    a
            }
        }


private fun markupName(name: String): String =
        name.fold("") { a, b ->
            if (b.isJavaIdentifierPart() || b == '.')
                a + b
            else
                a + "$" + java.lang.Integer.toHexString(b.toInt())
        }


private fun translateTypeDeclarations(declarations: List<za.co.no9.sle.ast.core.Declaration>): List<za.co.no9.sle.pass4.Declaration> =
        declarations.fold(emptyList()) { a, b ->
            when (b) {
                is TypeDeclaration ->
                    a + translateTypeDeclaration(b)
                else ->
                    a
            }
        }


private fun translateTypeDeclaration(declaration: TypeDeclaration): List<za.co.no9.sle.pass4.Declaration> =
        declaration.constructors.withIndex().fold(emptyList()) { declarations, indexedConstructor ->
            val constructorType =
                    indexedConstructor.value.arguments.foldRight(declaration.scheme.type) { typeA, typeB -> TArr(typeA, typeB) }

            declarations +
                    MemberDeclaration(null, true, true, true,
                            "${indexedConstructor.value.name.name}$",
                            "int",
                            IntegerLiteralExpression(indexedConstructor.index)) +
                    MemberDeclaration("${indexedConstructor.value.name.name}: ${generalise(constructorType).normalize()}", true, true, true,
                            indexedConstructor.value.name.name,
                            "java.lang.Object",
                            constructorBody(declaration, indexedConstructor.value))
        }


private fun constructorBody(declaration: TypeDeclaration, constructor: Constructor): za.co.no9.sle.pass4.Expression {
    data class ExpressionState(
            val argumentIndex: Int,
            val type: Type,
            val expression: za.co.no9.sle.pass4.Expression)

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

        val nextArgumentIndex =
                expressionState.argumentIndex - 1

        ExpressionState(
                nextArgumentIndex,
                argumentType,
                AnonymousObjectCreationExpression(AnonymousClassDeclaration("java.util.function.Function<java.lang.Object, java.lang.Object>", listOf(
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
        )
    }

    return finalExpressionState.expression
}


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
                        NameExpression(markupName(expression.name))

                    else ->
                        NameExpression(refMappingName)
                }
            }

            is IfExpression ->
                ConditionalExpression(TypeCastExpression("java.lang.Boolean", translate(expression.guardExpression)), translate(expression.thenExpression), translate(expression.elseExpression))

            is LambdaExpression ->
                AnonymousObjectCreationExpression(
                        AnonymousClassDeclaration("java.util.function.Function<java.lang.Object, java.lang.Object>", listOf(
                                za.co.no9.sle.pass4.MethodDeclaration(
                                        true,
                                        false,
                                        false,
                                        "apply",
                                        "java.lang.Object",
                                        listOf(Parameter(expression.argument.name, "java.lang.Object")),
                                        StatementBlock(listOf(
                                                ReturnStatement(translate(expression.expression))
                                        ))
                                )
                        ))
                )

            is CallExpression ->
                MethodCallExpression(
                        TypeCastExpression("java.util.function.Function<java.lang.Object, java.lang.Object>", translate(expression.operator)),
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
        }
