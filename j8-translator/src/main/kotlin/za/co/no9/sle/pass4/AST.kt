package za.co.no9.sle.pass4


data class CompilationUnit(
        val packageName: String,
        val imports: List<ImportDeclaration>,
        val declarations: List<Declaration>) {
    override fun toString(): String =
            "package $packageName;\n\n" +
                    imports.joinToString("") { it.toString() } + "\n" +
                    declarations.map { it.toString(0) }.joinToString("")
}


data class ImportDeclaration(
        val isStatic: Boolean,
        val name: String,
        val isAsterisk: Boolean) {
    override fun toString(): String =
            "import " +
                    (if (isStatic) "static " else "") +
                    name +
                    (if (isAsterisk) ".*" else "") +
                    ";\n"
}


sealed class Declaration {
    override fun toString(): String =
            toString(0)

    abstract fun toString(indent: Int): String

}

data class ClassDeclaration(
        val name: String,
        val declarations: List<Declaration>) : Declaration() {
    override fun toString(indent: Int): String =
            "${spaces(indent)}public class $name {\n" +
                    declarations.map { it.toString(indent + 1) }.joinToString("\n") +
                    "${spaces(indent)}}"
}

data class MemberDeclaration(
        val comment: String?,
        val isPublic: Boolean,
        val isStatic: Boolean,
        val isFinal: Boolean,
        val name: String,
        val type: String,
        val init: Expression?) : Declaration() {
    override fun toString(indent: Int): String =
            (if (comment == null) "" else "${spaces(indent)}/**\n${spaces(indent)} * $comment\n${spaces(indent)} */\n") +
                    spaces(indent) +
                    (if (isPublic) "public " else "") +
                    (if (isStatic) "static " else "") +
                    (if (isFinal) "final " else "") +
                    "$type $name" +
                    (if (init == null) "" else " = ${init.toString(indent)}") +
                    ";\n"
}

data class MethodDeclaration(
        val isPublic: Boolean,
        val isStatic: Boolean,
        val isFinal: Boolean,
        val name: String,
        val type: String,
        val parameters: List<Parameter>,
        val statements: StatementBlock) : Declaration() {
    override fun toString(indent: Int): String =
            spaces(indent) +
                    (if (isPublic) "public " else "") +
                    (if (isStatic) "static " else "") +
                    (if (isFinal) "final " else "") +
                    "$type $name(${parameters.map { it.toString() }.joinToString(", ")}) " +
                    statements.toString(indent)
}


data class StatementBlock(
        val statements: List<Statement>) {
    fun toString(indent: Int): String =
            "{\n" +
                    statements.map { it.toString(indent + 1) }.joinToString("") +
                    "${spaces(indent)}}\n"
}


sealed class Statement {
    abstract fun toString(indent: Int): String
}

data class ReturnStatement(
        val expression: Expression) : Statement() {
    override fun toString(indent: Int): String =
            "${spaces(indent)}return ${expression.toString(4)};\n"
}

data class BlockStatement(
        val statements: List<Statement>) : Statement() {
    override fun toString(indent: Int): String =
            "{\n" +
                    statements.map { it.toString(indent + 1) }.joinToString("") +
                    "${spaces(indent)}}\n"
}

data class ThrowStatement(
        val expression: Expression) : Statement() {
    override fun toString(indent: Int): String =
            "${spaces(indent)}throw ${expression.toString(indent + 1)};\n"
}

data class SwitchStatement(
        val selector: Expression,
        val entries: List<SwitchStatementEntry>,
        val defaultEntry: Statement?) : Statement() {
    override fun toString(indent: Int): String =
            "${spaces(indent)}switch(${selector.toString(indent + 1)}) {\n" +
                    entries.map { it.toString(indent + 1) }.joinToString("") +
                    (if (defaultEntry == null) "" else "${spaces(indent + 1)}default:\n${defaultEntry.toString(indent + 2)}") +
                    "${spaces(indent)}}\n"
}

data class VariableDeclarationStatement(
        val type: String,
        val name: String,
        val init: Expression?) : Statement() {
    override fun toString(indent: Int): String =
            "${spaces(indent)}$type $name" +
                    (if (init == null) "" else " = ${init.toString(indent + 1)}") +
                    ";\n"
}


data class SwitchStatementEntry(
        val guard: Expression,
        val statement: Statement) {
    fun toString(indent: Int): String =
            if (statement is BlockStatement)
                "${spaces(indent)}case ${guard.toString(indent + 1)}: ${statement.toString(indent)}"
            else
                "${spaces(indent)}case ${guard.toString(indent + 1)}:\n${statement.toString(indent + 1)}"
}

sealed class Expression {
    abstract fun toString(indent: Int): String
}


data class IntegerLiteralExpression(
        val value: Int) : Expression() {
    override fun toString(indent: Int) =
            value.toString()
}

data class BoolLiteralExpression(
        val value: Boolean) : Expression() {
    override fun toString(indent: Int) =
            value.toString()
}

data class StringLiteralExpression(
        val value: String) : Expression() {
    override fun toString(indent: Int) =
            "\"$value\""
}

data class NameExpression(
        val name: String) : Expression() {
    override fun toString(indent: Int) =
            name
}

data class ConditionalExpression(
        val guard: Expression,
        val thenExpression: Expression,
        val elseExpression: Expression) : Expression() {
    override fun toString(indent: Int) =
            guard.toString(indent) + " ? " + thenExpression.toString(indent) + " : " + elseExpression.toString(indent)
}

data class AnonymousObjectCreationExpression(
        val anonymousClassDeclaration: AnonymousClassDeclaration) : Expression() {
    override fun toString(indent: Int): String =
            "new ${anonymousClassDeclaration.toString(indent)}"
}

data class ObjectCreationExpression(
        val type: String,
        val argument: List<Expression>) : Expression() {
    override fun toString(indent: Int): String =
            "new $type(${argument.map { it.toString(indent + 1) }.joinToString(", ")})"
}

data class ArrayInitialisationExpression(
        val type: String,
        val argument: List<Expression>) : Expression() {
    override fun toString(indent: Int): String =
            "new $type { ${argument.map { it.toString(indent + 1) }.joinToString(", ")} }"
}

data class TypeCastExpression(
        val type: String,
        val expression: Expression) : Expression() {
    override fun toString(indent: Int): String =
            "(($type) ${expression.toString(indent)})"
}

data class OpExpression(
        val left: Expression,
        val op: String,
        val right: Expression) : Expression() {
    override fun toString(indent: Int): String =
            "(${left.toString(indent)} $op ${right.toString(indent)})"
}

data class MethodCallExpression(
        val expression: Expression,
        val name: String,
        val arguments: List<Expression>) : Expression() {
    override fun toString(indent: Int): String =
            "${expression.toString(indent)}.$name(${arguments.map { it.toString(indent + 1) }.joinToString(", ")})"
}

data class ArrayAccessExpression(
        val expression: Expression,
        val index: Expression) : Expression() {
    override fun toString(indent: Int): String =
            "${expression.toString(indent)}[${index.toString(indent)}]"
}

data class AnonymousClassDeclaration(
        val type: String,
        val declarations: List<Declaration>) : Declaration() {
    override fun toString(indent: Int): String =
            "$type() {\n" +
                    declarations.map { it.toString(indent + 1) }.joinToString("\n") +
                    "${spaces(indent)}}"
}


data class Parameter(
        val name: String,
        val type: String) {
    override fun toString() =
            "$type $name"
}


fun spaces(indent: Int): String =
        "    ".repeat(indent)