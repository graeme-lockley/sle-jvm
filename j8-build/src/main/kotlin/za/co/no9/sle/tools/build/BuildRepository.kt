package za.co.no9.sle.tools.build

import za.co.no9.sle.*
import za.co.no9.sle.pass4.toClass
import za.co.no9.sle.pass4.translate
import za.co.no9.sle.repository.toJsonString
import za.co.no9.sle.transform.enrichedCoreToCore.parse
import za.co.no9.sle.transform.typelessPatternToTypedPattern.importAll
import za.co.no9.sle.typing.*
import java.io.File


class BuildRepository(override val sourcePrefix: File,
                      override val targetRoot: File,
                      sourceFiles: Set<URN>) : Repository(sourcePrefix, targetRoot) {
    constructor(
            sourcePrefix: File,
            targetRoot: File) : this(sourcePrefix, targetRoot, sources(sourcePrefix))

    private val compiling =
            mutableSetOf<URN>()

    val compiled =
            mutableSetOf<URN>()

    val buildErrors =
            mutableMapOf<URN, Errors>()

    var environment =
            initialEnvironment
                    .newValue("i_BuiltinValue", VariableBinding(Scheme(listOf(1), TArr(typeString, TVar(homeLocation, 1)))))

    init {
        val preludeItem =
                item(URN(Resource, "/Prelude.sle")).right()!!


        val importAllResult =
                importAll(environment, homeLocation, preludeItem)

        environment =
                importAllResult.environment

        sourceFiles.forEach { urn ->
            val result =
                    item(urn)

            val errors =
                    result.left()

            if (errors != null) {
                includeErrors(urn, errors)
            }
        }
    }


    private fun includeErrors(urn: URN, errors: Errors) {
        if (buildErrors.containsKey(urn)) {
            buildErrors[urn] = buildErrors[urn]!! + errors
        } else {
            buildErrors[urn] = errors
        }
    }


    override fun itemLoaded(item: Item) {
        if (compiling.contains(item.sourceURN())) {
            includeErrors(item.sourceURN(), setOf(CyclicDependency(item.sourceURN())))
        } else if (!compiled.contains(item.sourceURN())) {
            compiling.add(item.sourceURN())

            val packageName =
                    item.javaPackageName

            val className =
                    item.className

            val parseDetail =
                    parse(item, environment)

            val compiledFile =
                    parseDetail.map { translate(it.environment, it.module, packageName, className) }

            val output =
                    compiledFile.map { it.toString() }

            val errors =
                    output.left()

            if (errors == null) {
                item.writeJava(output.right() ?: "")
                item.writeJson(toJsonString(toClass(parseDetail.right()!!.environment, item, parseDetail.right()!!.module.exports)))
            } else {
                includeErrors(item.sourceURN(), errors)
            }

            compiling.remove(item.sourceURN())
            compiled.add(item.sourceURN())
        }
    }
}


fun sources(directory: File) =
        directory.walk().filter { it.isFile }.filter { it.name.endsWith(".sle") }.map { URN(it) }.toSet()
