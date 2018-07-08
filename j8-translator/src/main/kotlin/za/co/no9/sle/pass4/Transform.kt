package za.co.no9.sle.pass4

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.comments.JavadocComment
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.stmt.ReturnStmt
import com.github.javaparser.ast.type.ClassOrInterfaceType
import za.co.no9.sle.typing.TArr
import za.co.no9.sle.typing.TCon
import za.co.no9.sle.typing.TVar
import za.co.no9.sle.typing.Type
import za.co.no9.sle.pass3.*
import za.co.no9.sle.pass3.Expression


private val RefMapping = mapOf(
        Pair("(==)", "EQUAL_EQUAL"),
        Pair("(-)", "MINUS"),
        Pair("(*)", "STAR")
)


fun translateToJava(module: Module, packageDeclaration: String, className: String): CompilationUnit {
    val compilationUnit =
            CompilationUnit()

    if (packageDeclaration.isNotEmpty()) {
        compilationUnit.setPackageDeclaration(packageDeclaration)
    }
    compilationUnit.addImport("java.util.function.Function", false, false)
    compilationUnit.addImport("za.co.no9.sle.runtime.Builtin", true, true)

    val thingy =
            compilationUnit.addClass(className)
                    .setPublic(true)

    for (declaration in module.declarations) {
        when (declaration) {
            is LetDeclaration -> {
                thingy.addOrphanComment(JavadocComment("${declaration.name.name}: ${declaration.type}"))

                thingy.addFieldWithInitializer(
                        javaType(declaration.type),
                        declaration.name.name,
                        javaExpression(declaration.expression),
                        Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            }
        }
    }

    return compilationUnit
}


private fun javaType(type: Type): String =
        when (type) {
            is TCon ->
                when {
                    type.name == "Int" ->
                        "Integer"

                    type.name == "Bool" ->
                        "Boolean"

                    else ->
                        type.name
                }

            is TVar ->
                type.variable.toString()

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
                        javaExpression(expression.operand),
                        "apply",
                        NodeList.nodeList(javaExpression(expression.operator)))
        }

