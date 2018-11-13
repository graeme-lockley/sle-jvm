package za.co.no9.sle.transform.enrichedCoreToCore

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.specs.FunSpec
import za.co.no9.sle.*
import za.co.no9.sle.ast.core.*
import za.co.no9.sle.typing.*
import java.util.function.Consumer


class EnrichedCoreToCoreTests : FunSpec({
    runner(this, "enrichedCoreToCore", RunnerConsumer())
})


private class RunnerConsumer : Consumer<Map<String, List<String>>> {
    private val environment =
            initialEnvironment
                    .newValue("(+)", Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))
                    .newValue("(-)", Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))
                    .newValue("(*)", Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))
                    .newValue("(/)", Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))
                    .newValue("(&&)", Scheme(listOf(), TArr(typeBool, TArr(typeBool, typeBool))))
                    .newValue("(==)", Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool))))
                    .newValue("aString", Scheme(listOf(), typeString))


    override fun accept(fileContent: Map<String, List<String>>) {
        val parseWithDetail =
                parseWithDetail(fileContent["src"]?.joinToString("\n") ?: "", environment)


        val constraints =
                fileContent["constraints"]

        if (constraints != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Detail>>()
            parseWithDetail.right()!!.constraints.state.map { it.toString() }.shouldBeEqual(constraints)
        }


        val expectedSubstitution =
                fileContent["substitution"]

        if (expectedSubstitution != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Detail>>()
            parseWithDetail.right()!!.substitution.state.map { it.toString() }.shouldBeEqual(expectedSubstitution)
        }


        val astTest =
                fileContent["ast"]

        if (astTest != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Detail>>()
            parseWithDetail.right()!!.unresolvedModule.shouldBeEqual(astTest)
        }


        val typeAST =
                fileContent["typeAST"]

        if (typeAST != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Detail>>()
            parseWithDetail.right()!!.resolvedModule.shouldBeEqual(typeAST)
        }


        val coreAST =
                fileContent["coreAST"]

        if (coreAST != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Detail>>()
            parseWithDetail.right()!!.coreModule.shouldBeEqual(coreAST)
        }


        val coreASTpp =
                fileContent["coreASTpp"]

        if (coreASTpp != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Detail>>()
            parseWithDetail.right()!!.coreModule.asString().shouldBeEqual(coreASTpp)
        }


        val errors =
                fileContent["errors"]

        if (errors != null) {
            parseWithDetail.shouldBeTypeOf<Either.Error<Detail>>()
            parseWithDetail.left()!!.map { it.toString() }.shouldBeEqual(errors)
        }
    }
}


fun Module.asString(): String =
        this.exports.asString() +
                this.declarations.joinToString("\n") { it.asString() }

fun List<NameDeclaration>.asString(): String =
        if (this.isEmpty())
            ""
        else
            this.joinToString("") { it.asString() } + "\n"


fun NameDeclaration.asString(): String =
        when (this) {
            is ValueNameDeclaration ->
                "export value ${this.name}: ${this.scheme}\n"

            is AliasNameDeclaration ->
                "export alias ${this.name}: ${this.scheme}\n"

            is ADTNameDeclaration ->
                "export adt ${this.name}: ${this.scheme}\n"

            is FullADTNameDeclaration ->
                "export full ${this.name}: ${this.scheme}\n${this.constructors.joinToString("\n") { "  ${it.name}: ${it.scheme}" }}\n"
        }


fun Declaration.asString(): String =
        when (this) {
            is LetDeclaration ->
                "${name.name} : ${scheme.normalize()}\n${name.name} =\n${expression.asString(2)}\n"

            is TypeAliasDeclaration ->
                "typealias ${name.name} =\n  $scheme\n"

            is TypeDeclaration ->
                "type ${name.name} =\n  " + constructors.joinToString("| ") { it.asString() } + "\n"
        }


fun Constructor.asString(): String =
        "${name.name}${arguments.joinToString("") { " " + it.toString() }}\n"


fun Expression.asString(indent: Int = 0): String =
        when (this) {
            is za.co.no9.sle.ast.core.Unit ->
                "${spaces(indent)}()\n"

            is ConstantBool ->
                spaces(indent) + value.toString() + "\n"

            is ConstantInt ->
                spaces(indent) + value.toString() + "\n"

            is ConstantString ->
                "${spaces(indent)}\"$value\"\n"

            is FAIL ->
                "${spaces(indent)}FAIL\n"

            is ERROR ->
                "${spaces(indent)}ERROR\n"

            is IdReference ->
                spaces(indent) + name + "\n"

            is IfExpression ->
                "${spaces(indent)}(IF\n" +
                        guardExpression.asString(indent + 2) +
                        thenExpression.asString(indent + 2) +
                        elseExpression.asString(indent + 2) +
                        "${spaces(indent)})\n"

            is LambdaExpression ->
                "${spaces(indent)}(LAMBDA ${argument.name} ->\n${expression.asString(indent + 2)}${spaces(indent)})\n"

            is CallExpression ->
                "${spaces(indent)}(CALL\n${operator.asString(indent + 2)}${operand.asString(indent + 2)}${spaces(indent)})\n"

            is CaseExpression ->
                "${spaces(indent)}(CASE $variable\n${clauses.joinToString("") { it.asString(indent + 2) }}${spaces(indent)})\n"
        }

fun CaseExpressionClause.asString(indent: Int): String =
        "${spaces(indent)}$constructorName${variables.joinToString("") { " $it" }} => \n${expression.asString(indent + 2)}"