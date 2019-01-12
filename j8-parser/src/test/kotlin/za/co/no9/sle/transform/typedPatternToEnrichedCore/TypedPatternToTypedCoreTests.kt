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


private class RunnerConsumer : Consumer<ConsumerParam> {
    private val environment =
            initialEnvironment
                    .newValue("+",
                            OperatorBinding(
                                    Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))),
                                    6,
                                    Left
                            ))
                    .newValue("-",
                            OperatorBinding(
                                    Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))),
                                    6,
                                    Left
                            ))
                    .newValue("*",
                            OperatorBinding(
                                    Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))),
                                    7,
                                    Left
                            ))
                    .newValue("/",
                            OperatorBinding(
                                    Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))),
                                    7,
                                    Left
                            ))
                    .newValue("&&",
                            OperatorBinding(
                                    Scheme(listOf(), TArr(typeBool, TArr(typeBool, typeBool))),
                                    3,
                                    Right
                            ))
                    .newValue("==",
                            OperatorBinding(
                                    Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool))),
                                    4,
                                    None
                            ))
                    .newValue("aString", VariableBinding(Scheme(listOf(), typeString)))


    override fun accept(param: ConsumerParam) {
        val sourceFile =
                param.first

        val fileContent =
                param.second

        val parseWithDetail =
                parseWithDetail(TestItem(sourceFile, fileContent["src"]?.joinToString("\n")
                        ?: ""), environment)


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

            is OperatorNameDeclaration ->
                "export operator ${this.name}: ${this.scheme}: ${this.precedence}: ${this.associativity}\n"

            is AliasNameDeclaration ->
                "export alias ${this.name}: ${this.scheme}\n"

            is ADTNameDeclaration ->
                "export adt ${this.name}: ${this.scheme}\n"

            is FullADTNameDeclaration ->
                "export full ${this.name}: ${this.scheme}\n${this.constructors.joinToString("\n") { "  ${it.name}: ${it.scheme}" }}\n"
        }


fun Declaration.asString(indent: Int = 0): String =
        when (this) {
            is LetDeclaration ->
                "${spaces(indent)}${id.name.name} : ${scheme.normalize()}\n${spaces(indent)}${id.name.name} =\n${expression.asString(indent + 2)}\n"

            is TypeAliasDeclaration ->
                "${spaces(indent)}typealias ${name.name} =\n  $scheme\n"

            is TypeDeclaration ->
                "${spaces(indent)}type ${name.name} =\n  " + constructors.joinToString("| ") { it.asString() } + "\n"
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

            is ConstantChar ->
                "${spaces(indent)}'$value'\n"

            is FAIL ->
                "${spaces(indent)}FAIL\n"

            is ERROR ->
                "${spaces(indent)}ERROR\n"

            is IdReference ->
                spaces(indent) + name + "\n"

            is LetExpression ->
                "${spaces(indent)}(LET\n" +
                        this.declarations.joinToString("\n") { it.asString(indent + 2) } +
                        expression.asString(indent + 2) +
                        "${spaces(indent)})\n"

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
                "${spaces(indent)}(BAR\n${expressions.joinToString("") { it.asString(indent + 2) }}${spaces(indent)})\n"
        }


fun Pattern.asString(): String =
        when (this) {
            is IdReferencePattern ->
                name

            is IgnorePattern ->
                "_"

            is ConstructorReferencePattern ->
                "$name${parameters.joinToString("") { " " + it.type.toString() }}"
        }