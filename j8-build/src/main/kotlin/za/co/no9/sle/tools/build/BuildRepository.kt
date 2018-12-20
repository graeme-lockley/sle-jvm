package za.co.no9.sle.tools.build

import za.co.no9.sle.*
import za.co.no9.sle.pass4.toClass
import za.co.no9.sle.pass4.translate
import za.co.no9.sle.repository.toJsonString
import za.co.no9.sle.transform.enrichedCoreToCore.parseWithDetail
import za.co.no9.sle.transform.typelessPatternToTypedPattern.importAll
import za.co.no9.sle.typing.*
import java.io.File

class BuildRepository(override val sourcePrefix: File,
                      override val targetRoot: File) : Repository(sourcePrefix, targetRoot) {
    private val files =
            sourcePrefix.walk().filter { it.isFile }.filter { it.name.endsWith(".sle") }.map { URN(it) }.toSet()

    private val compiling =
            mutableSetOf<String>()

    val compiled =
            mutableSetOf<String>()

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

//        println(environment)

        files.forEach { urn ->
            //            println("Attempting $urn")

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
        if (compiling.contains(item.className)) {
            includeErrors(item.sourceURN(), setOf(CyclicDependency(item.sourceURN())))
        } else if (!compiled.contains(item.className)) {
//            println("  - ${item.packageName.joinToString(".")}.${item.className}")

            compiling.add(item.className)

            val packageName =
                    item.packageName.joinToString(".")

            val className =
                    item.className

            val parseDetail =
                    parseWithDetail(item, environment)

            val compiledFile =
                    parseDetail
                            .map { translate(it.coreModule, packageName, className) }

            val output =
                    compiledFile
                            .map { it.toString() }

            val errors =
                    output.left()

            if (errors == null) {
                item.writeJava(output.right() ?: "")
                item.writeJson(toJsonString(toClass(item, parseDetail.right()!!.coreModule.exports)))
            } else {
                includeErrors(item.sourceURN(), errors)
            }

            compiling.remove(item.className)
            compiled.add(item.className)
        }
    }
}