package za.co.no9.sle.tools.build

import za.co.no9.sle.*
import za.co.no9.sle.pass4.toClass
import za.co.no9.sle.pass4.translate
import za.co.no9.sle.repository.toJsonString
import za.co.no9.sle.transform.enrichedCoreToCore.parseWithDetail
import za.co.no9.sle.transform.typelessPatternToTypedPattern.importAll
import za.co.no9.sle.typing.*
import java.io.File


interface Log {
    fun error(message: String)
    fun info(message: String)
}


class BuildException(val source: File, shortMessage: String, val longMessage: String) : Exception(shortMessage)


fun build(log: Log, sourceFile: File, targetFile: File) {
    if (!sourceFile.isDirectory) {
        log.error("Source $sourceFile is not a valid directory")
        return
    }

    targetFile.mkdirs()
    if (!targetFile.isDirectory) {
        log.error("Unable to access target $targetFile directory")
        return
    }

    val repository =
            BuildRepository(sourceFile, targetFile)

    log.info("Compiled ${repository.compiled.size} file(s)")


    var errorCount =
            0

    for (buildError in repository.buildErrors) {
        if (buildError.value.isNotEmpty()) {
            buildError.value.forEach { error ->
                errorCount += 1

                val sourceName =
                        buildError.key

                val errorMessage =
                        when (error) {
                            is SyntaxError ->
                                "Syntax Error: $sourceName: ${error.location}: ${error.msg}"

                            is UnboundVariable ->
                                "Unbound Parameter: $sourceName: ${error.location}: ${error.name}"

                            is DuplicateLetDeclaration ->
                                "Duplicate Let Declaration: $sourceName: ${error.location}: ${error.name}"

                            is DuplicateLetSignature ->
                                "Duplicate Let Signature: $sourceName: ${error.location}: ${error.otherLocation}: ${error.name}"

                            is UnificationFail ->
                                "Unification Fail: $sourceName: ${error.t1} ${error.t2}"

                            is UnificationMismatch ->
                                "Unification Mismatch: $sourceName: ${error.t1s}: ${error.t2s}"

                            is UnknownTypeReference ->
                                "Unknown Type Reference: $sourceName: ${error.location}: ${error.name}"

                            is UnknownConstructorReference ->
                                "Unknown Constructor Reference: $sourceName: ${error.location}: ${error.name}"

                            is DuplicateTypeDeclaration ->
                                "Duplicate Type Declaration: $sourceName: ${error.location}: ${error.name}"

                            is DuplicateTypeAliasDeclaration ->
                                "Duplicate Type Alias Declaration: $sourceName: ${error.location}: ${error.name}"

                            is DuplicateConstructorDeclaration ->
                                "Duplicate Constructor Declaration: $sourceName: ${error.location}: ${error.name}"

                            is LetSignatureWithoutDeclaration ->
                                "Let Signature Without Declaration: $sourceName: ${error.location}: ${error.name}"

                            is IncorrectNumberOfSchemeArguments ->
                                "Incorrect Number of Scheme Arguments: $sourceName: ${error.location}: ${error.name}: actual ${error.actual}: expected ${error.expected}"

                            is IncorrectNumberOfAliasArguments ->
                                "Incorrect Number of Alias Arguments: $sourceName: ${error.location}: ${error.name}: actual ${error.actual}: expected ${error.expected}"

                            is IncorrectNumberOfConstructorArguments ->
                                "Incorrect Number of Constructor Arguments: $sourceName: ${error.location}: ${error.name}: actual ${error.actual}: expected ${error.expected}"

                            is IncompatibleDeclarationSignature ->
                                "Incompatible Declaration Signature: $sourceName: ${error.location}: ${error.name}: inferred ${error.inferred}: expected ${error.expected}"

                            is UnknownType ->
                                "Unknown Type: $sourceName: ${error.location}: ${error.name}"

                            is NonExhaustivePattern ->
                                "Non Exhaustive Pattern: $sourceName: ${error.location}"

                            is ValueNotExported ->
                                "Value not Exported: $sourceName: ${error.location}: ${error.name}"

                            is TypeNotExported ->
                                "Type not Exported: $sourceName: ${error.location}: ${error.name}"

                            is TypeAliasHasNoConstructors ->
                                "Type Alias has no Constructors: $sourceName: ${error.location}: ${error.name}"

                            is ADTHasNoConstructors ->
                                "ADT has no Constructors: $sourceName: ${error.location}: ${error.name}"

                            is TypeConstructorNotExported ->
                                "Type Constructor not Exported: $sourceName: ${error.location}: ${error.name}"

                            is ImportErrors ->
                                "Import Errors: $sourceName: ${error.location}:\n${error.errors}"

                            is UnableToReadFile ->
                                "Unable to read file: $sourceName: ${error.file}"

                            is CyclicDependency ->
                                "Cyclic Dependency: $sourceName: ${error.file}"

                            is DuplicateImportedTypeDeclaration ->
                                "Duplicate Imported Type Name: $sourceName: ${error.location}: ${error.name}"

                            is DuplicateImportedTypeAliasDeclaration ->
                                "Duplicate Imported Type Alias Name: $sourceName: ${error.location}: ${error.name}"

                            is DuplicateImportedLetDeclaration ->
                                "Duplicate Imported Value Name: $sourceName: ${error.location}: ${error.name}"

                            is DuplicateImportedName ->
                                "Duplicate Imported Name: $sourceName: ${error.location}: ${error.name}"

                            is IOException ->
                                "IO Exception: $sourceName: ${error.e.message}: ${error.e.stackTrace}"
                        }

                log.error(errorMessage)
            }
        }
    }

    if (errorCount > 0)
        throw BuildException(sourceFile, "Translation Error", "$errorCount errors")
}


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