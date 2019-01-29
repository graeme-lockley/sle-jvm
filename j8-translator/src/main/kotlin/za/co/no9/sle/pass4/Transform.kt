package za.co.no9.sle.pass4

import za.co.no9.sle.ast.core.*
import za.co.no9.sle.ast.core.Expression
import za.co.no9.sle.ast.core.Unit
import za.co.no9.sle.typing.*


private val RefMapping = mapOf(
        Pair("i_BuiltinValue", "za.co.no9.sle.runtime.Builtin.BUILTIN_VALUE")
)


fun translate(environment: Environment, module: Module, packageDeclaration: String, className: String): za.co.no9.sle.pass4.CompilationUnit {
    val optimizedModule =
            optimize(module)

    return za.co.no9.sle.pass4.CompilationUnit(
            packageDeclaration,
            listOf(),
            listOf(ClassDeclaration(className, translateTypeDeclarations(optimizedModule.declarations) + translateLetDeclarations(environment, optimizedModule.declarations))))
}


private fun translateLetDeclarations(environment: Environment, declarations: List<za.co.no9.sle.ast.core.Declaration>): List<Declaration> =
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

                    a + MemberDeclaration("$declarationCommentName: ${declaration.scheme.normalize()}", true, true, true, markupName(declaration.id.name.name), "java.lang.Object", translate(environment, declaration.expression))
                }

                else ->
                    a
            }
        }


private fun markupName(name: String): String =
        if (name == "_")
            "\$underscore"
        else
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


private fun translate(environment: Environment, expression: Expression): za.co.no9.sle.pass4.Expression =
        when (expression) {
            is Unit ->
                NameExpression("za.co.no9.sle.runtime.Unit.INSTANCE")

            is ConstantBool ->
                BoolLiteralExpression(expression.value)

            is ConstantInt ->
                IntegerLiteralExpression(expression.value)

            is ConstantString ->
                StringLiteralExpression(expression.value)

            is ConstantChar ->
                CharLiteralExpression(expression.value)

            is ConstantRecord -> {
                val indexFields =
                        expression.fields.sortedBy { it.name.name }

                ArrayInitialisationExpression(
                        "java.lang.Object[]",
                        indexFields.map { translate(environment, it.value) }
                )
            }

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

            is LetExpression ->
                MethodCallExpression(
                        AnonymousObjectCreationExpression(
                                AnonymousClassDeclaration(
                                        "java.util.function.Supplier<java.lang.Object>",
                                        listOf(za.co.no9.sle.pass4.MethodDeclaration(
                                                true, false, false,
                                                "get",
                                                "java.lang.Object",
                                                emptyList(),
                                                StatementBlock(
                                                        expression.declarations.map {
                                                            VariableDeclarationStatement("java.lang.Object", it.id.name.name, translate(environment, it.expression))
                                                        } + ReturnStatement(translate(environment, expression.expression))
                                                ))
                                        ))
                        ),
                        "get",
                        emptyList())

            is IfExpression ->
                ConditionalExpression(TypeCastExpression("java.lang.Boolean", translate(environment, expression.guardExpression)), translate(environment, expression.thenExpression), translate(environment, expression.elseExpression))

            is LambdaExpression ->
                AnonymousObjectCreationExpression(
                        AnonymousClassDeclaration("java.util.function.Function<java.lang.Object, java.lang.Object>", listOf(
                                za.co.no9.sle.pass4.MethodDeclaration(
                                        true,
                                        false,
                                        false,
                                        "apply",
                                        "java.lang.Object",
                                        listOf(Parameter(markupName(expression.argument.name), "java.lang.Object")),
                                        StatementBlock(listOf(
                                                ReturnStatement(translate(environment, expression.expression))
                                        ))
                                )
                        ))
                )

            is CallExpression -> {
                fun standard() =
                        MethodCallExpression(
                                TypeCastExpression("java.util.function.Function<java.lang.Object, java.lang.Object>", translate(environment, expression.operator)),
                                "apply",
                                listOf(translate(environment, expression.operand)))

                if (expression.operand.type is TRec) {
//                    println ("Call TREC: ${(expression.operator.type as TArr).domain} -- ${expression.operand.type}")

                    val domainFields =
                            (resolveAlias(environment, (expression.operator.type as TArr).domain) as TRec).fields.sortedBy { it.first }

                    val argumentFields =
                            (expression.operand.type as TRec).fields.sortedBy { it.first }

                    if (domainFields.size == argumentFields.size)
                        standard()
                    else {
                        val domainFieldNames =
                                domainFields.map { it.first }.toSet()

                        val argumentIndexes =
                                argumentFields.mapIndexed { index, pair -> Pair(index, pair.first) }.filter { domainFieldNames.contains(it.second) }.map { it.first }

                        MethodCallExpression(
                                AnonymousObjectCreationExpression(
                                        AnonymousClassDeclaration("java.util.function.Function<java.lang.Object[], java.lang.Object>", listOf(
                                                za.co.no9.sle.pass4.MethodDeclaration(
                                                        true,
                                                        false,
                                                        false,
                                                        "apply",
                                                        "java.lang.Object",
                                                        listOf(Parameter("\$fromRecord", "java.lang.Object[]")),
                                                        StatementBlock(listOf(
                                                                VariableDeclarationStatement(
                                                                        "java.lang.Object[]",
                                                                        "\$toRecord",
                                                                        ArrayInitialisationExpression(
                                                                                "java.lang.Object[]",
                                                                                argumentIndexes.map { ArrayAccessExpression(NameExpression("\$fromRecord"), IntegerLiteralExpression(it)) }
                                                                        )
                                                                ),
                                                                ReturnStatement(
                                                                        MethodCallExpression(
                                                                                TypeCastExpression("java.util.function.Function<java.lang.Object, java.lang.Object>", translate(environment, expression.operator)),
                                                                                "apply",
                                                                                listOf(NameExpression("\$toRecord")))
                                                                )
                                                        ))
                                                )
                                        ))
                                ),
                                "apply",
                                listOf(TypeCastExpression("java.lang.Object[]", translate(environment, expression.operand))))
                    }
                } else
                    standard()
            }

            is FieldProjectionExpression -> {
                val fields =
                        (resolveAlias(environment, expression.record.type) as TRec).fields.map { it.first }.sorted()

                val fieldIndex =
                        fields.indexOf(expression.name.name)

                ArrayAccessExpression(TypeCastExpression("java.lang.Object[]", translate(environment, expression.record)), IntegerLiteralExpression(fieldIndex))
            }

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
                                        ReturnStatement(translate(environment, it.expression))
                                    else
                                        BlockStatement(
                                                it.variables.mapIndexed { index, s ->
                                                    VariableDeclarationStatement("java.lang.Object", s, ArrayAccessExpression(NameExpression(fullSelectorName), IntegerLiteralExpression(index + 1)))
                                                }.filter { variableDeclarationStatement -> variableDeclarationStatement.name != "_" } + ReturnStatement(translate(environment, it.expression))
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
