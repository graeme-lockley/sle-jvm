package za.co.no9.sle.transform.typedPatternToEnrichedCore

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.specs.FunSpec
import za.co.no9.sle.*
import za.co.no9.sle.ast.enrichedCore.*
import za.co.no9.sle.typing.*
import java.util.function.Consumer


class TypedPatternToEnrichedCoreTests : FunSpec({
    runner(this, "typedPatternToEnrichedCore", RunnerConsumer())
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


        val enrichedAST =
                fileContent["enrichedAST"]

        if (enrichedAST != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Detail>>()
            parseWithDetail.right()!!.enrichedModule.shouldBeEqual(enrichedAST)
        }


        val enrichedASTpp =
                fileContent["enrichedASTpp"]

        if (enrichedASTpp != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Detail>>()
            parseWithDetail.right()!!.enrichedModule.asString().shouldBeEqual(enrichedASTpp)
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
        this.declarations.map { it.asString() }.joinToString("\n")

fun Declaration.asString(): String =
        when (this) {
            is LetDeclaration ->
                "${name.name} : ${scheme.normalize()}\n${name.name} =\n${expression.asString(2)}\n"

            is TypeAliasDeclaration ->
                "typealias ${name.name} =\n  $scheme\n"

            is TypeDeclaration ->
                "type ${name.name} =\n  " + constructors.map { it.asString() }.joinToString("| ") + "\n"
        }


fun Constructor.asString(): String =
        "${name.name}${arguments.joinToString("") { " " + it.toString() }}\n"


fun Expression.asString(indent: Int = 0): String =
        when (this) {
            is za.co.no9.sle.ast.enrichedCore.Unit ->
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
                "${spaces(indent)}(LAMBDA ${argument.asString()} ->\n${expression.asString(indent + 2)}${spaces(indent)})\n"

            is CallExpression ->
                "${spaces(indent)}(CALL\n${operator.asString(indent + 2)}${operand.asString(indent + 2)}${spaces(indent)})\n"

            is Bar ->
                "${spaces(indent)}(BAR\n${expressions.map{it.asString(indent + 2)}.joinToString("")}${spaces(indent)})\n"
        }


fun Pattern.asString(): String =
        when (this) {
            is IdReferencePattern ->
                name

            is ConstructorReferencePattern ->
                "$name${parameters.joinToString("") { " " + it.type.toString() }}"
        }