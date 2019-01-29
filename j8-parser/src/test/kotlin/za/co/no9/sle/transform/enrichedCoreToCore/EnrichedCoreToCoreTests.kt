package za.co.no9.sle.transform.enrichedCoreToCore

import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.specs.FunSpec
import za.co.no9.sle.*
import za.co.no9.sle.ast.core.*
import za.co.no9.sle.transform.typelessPatternToTypedPattern.Constraints
import za.co.no9.sle.typing.*
import java.util.function.Consumer


class EnrichedCoreToCoreTests : FunSpec({
    runner(this, "enrichedCoreToCore", RunnerConsumer())
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

        val callback =
                TestParseCallback()

        val parseWithDetail =
                parse(TestItem(sourceFile, fileContent["src"]?.joinToString("\n")
                        ?: ""), environment, callback)


        val constraints =
                fileContent["constraints"]

        if (constraints != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Any>>()
            callback.constraints!!.state.map { it.toString() }.shouldBeEqual(constraints)
        }


        val expectedSubstitution =
                fileContent["substitution"]

        if (expectedSubstitution != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Any>>()
            callback.substitution!!.state.map { it.toString() }.shouldBeEqual(expectedSubstitution)
        }


        val astTest =
                fileContent["ast"]

        if (astTest != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Any>>()
            callback.unresolvedModule!!.shouldBeEqual(astTest)
        }


        val typeAST =
                fileContent["typeAST"]

        if (typeAST != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Any>>()
            callback.resolvedTypedPatternModule!!.shouldBeEqual(typeAST)
        }


        val coreAST =
                fileContent["coreAST"]

        if (coreAST != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Any>>()
            parseWithDetail.right()!!.shouldBeEqual(coreAST)
        }


        val coreASTpp =
                fileContent["coreASTpp"]

        if (coreASTpp != null) {
            parseWithDetail.shouldBeTypeOf<Either.Value<Any>>()
            parseWithDetail.right()!!.module.asString().shouldBeEqual(coreASTpp)
        }


        val errors =
                fileContent["errors"]

        if (errors != null) {
            parseWithDetail.shouldBeTypeOf<Either.Error<Any>>()
            parseWithDetail.left()!!.map { it.toString() }.shouldBeEqual(errors)
        }
    }
}


class TestParseCallback : za.co.no9.sle.transform.enrichedCoreToCore.ParseCallback {
    var resolvedTypedPatternModule: za.co.no9.sle.ast.typedPattern.Module? =
            null

    var unresolvedModule: za.co.no9.sle.ast.typedPattern.Module? =
            null

    var enrichedCoreModule: za.co.no9.sle.ast.enrichedCore.Module? =
            null

    var constraints: Constraints? =
            null

    var substitution: Substitution? =
            null

    var environment: Environment? =
            null


    override fun resolvedTypedPatternModule(resolvedTypedPatternModule: za.co.no9.sle.ast.typedPattern.Module) {
        this.resolvedTypedPatternModule = resolvedTypedPatternModule
    }

    override fun unresolvedTypedPatternModule(unresolvedTypedPatternModule: za.co.no9.sle.ast.typedPattern.Module) {
        this.unresolvedModule = unresolvedTypedPatternModule
    }

    override fun enrichedCoreModule(enrichedCoreModule: za.co.no9.sle.ast.enrichedCore.Module) {
        this.enrichedCoreModule = enrichedCoreModule
    }

    override fun constraints(constraints: Constraints) {
        this.constraints = constraints
    }

    override fun substitution(substitution: Substitution) {
        this.substitution = substitution
    }

    override fun environment(environment: Environment) {
        this.environment = environment
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
            is za.co.no9.sle.ast.core.Unit ->
                "${spaces(indent)}()\n"

            is ConstantBool ->
                spaces(indent) + value.toString() + "\n"

            is ConstantInt ->
                spaces(indent) + value.toString() + "\n"

            is ConstantString ->
                "${spaces(indent)}\"$value\"\n"

            is ConstantChar ->
                "${spaces(indent)}'$value'\n"

            is ConstantRecord ->
                "${spaces(indent)}{\n" +
                        fields.joinToString("") { "${spaces(indent + 1)}${it.name.name} =\n${it.value.asString(indent + 2)}" } +
                        "${spaces(indent)}}\n"

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
                "${spaces(indent)}(LAMBDA ${argument.name} ->\n${expression.asString(indent + 2)}${spaces(indent)})\n"

            is CallExpression ->
                "${spaces(indent)}(CALL\n${operator.asString(indent + 2)}${operand.asString(indent + 2)}${spaces(indent)})\n"

            is FieldProjectionExpression ->
                "${record.asString(indent)}${spaces(indent + 1)}.${name.name})\n"

            is CaseExpression ->
                "${spaces(indent)}(CASE $variable\n${clauses.joinToString("") { it.asString(indent + 2) }}${spaces(indent)})\n"
        }

fun CaseExpressionClause.asString(indent: Int): String =
        "${spaces(indent)}$constructorName${variables.joinToString("") { " $it" }} => \n${expression.asString(indent + 2)}"
