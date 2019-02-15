package za.co.no9.sle.tools.build

import za.co.no9.sle.*
import za.co.no9.sle.typing.*
import java.io.File


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


    val errorCount =
            displayBuildErrors(log, repository.buildErrors)

    if (errorCount > 0)
        throw BuildException(sourceFile, "Translation Error", "$errorCount errors")
}


fun displayBuildErrors(log: Log, buildErrors: Map<URN, Errors>): Int {
    var errorCount =
            0

    val currentDir =
            java.io.File(".").canonicalFile

    fun ppSourceName(urn: URN): String =
            when (urn.source) {
                File ->
                    try {
                        val sourceFile =
                                java.io.File(urn.name)

                        sourceFile.relativeTo(currentDir).toString()
                    } catch (e: Exception) {
                        urn.name
                    }

                Github ->
                    "github:" + urn.name + (if (urn.version == null) "" else ":${urn.version}")

                Resource ->
                    "resource:" + urn.name
            }


    fun commonPrefix(s1: String?, s2: String?): String? =
            when {
                s1 == null ->
                    s2

                s2 == null ->
                    s1

                else -> {
                    s1.commonPrefixWith(s2)
                }
            }

    fun calculatePrefix(type: Type): String? =
            when (type) {
                is TCon -> {
                    val initial: String? =
                            when {
                                type.name.startsWith("Data.") || type.name.startsWith("resource.Prelude.") ->
                                    null

                                else -> {
                                    type.name
                                }
                            }

                    type.arguments.fold(initial) { accumulator, t ->
                        commonPrefix(accumulator, calculatePrefix(t))
                    }
                }

                is TVar ->
                    null

                is TAlias ->
                    null

                is TArr ->
                    commonPrefix(calculatePrefix(type.domain), calculatePrefix(type.range))

                is TRec -> {
                    val initial: String? =
                            null

                    type.fields.fold(initial) { accumulator, field ->
                        commonPrefix(accumulator, calculatePrefix(field.second))
                    }
                }
            }


    fun ppType(prefix: String, type: Type): Type =
            when (type) {
                is TCon -> {
                    val name =
                            when {
                                type.name.startsWith("Data.") ->
                                    type.name.drop(5)

                                type.name.startsWith("resource.Prelude.") ->
                                    type.name.drop(17)

                                type.name == prefix -> {
                                    val indexOf =
                                            type.name.lastIndexOf(".")

                                    if (indexOf == -1)
                                        type.name
                                    else
                                        type.name.substring(indexOf + 1)
                                }

                                type.name.startsWith(prefix) ->
                                    type.name.drop(prefix.length)

                                else ->
                                    type.name
                            }

                    TCon(type.location, name, type.arguments.map { ppType(prefix, it) })
                }

                is TVar ->
                    type

                is TAlias ->
                    TAlias(type.location, type.name, type.arguments.map { ppType(prefix, it) })

                is TArr ->
                    TArr(type.location, ppType(prefix, type.domain), ppType(prefix, type.range))

                is TRec ->
                    TRec(type.location, type.fixed, type.fields.map { Pair(it.first, ppType(prefix, it.second)) })
            }


    fun ppType(type: Type): Type =
            ppType(calculatePrefix(type) ?: "", type)


    fun ppScheme(scheme: Scheme): Scheme {
        val normalizedScheme =
                scheme.normalize()

        return Scheme(normalizedScheme.parameters, ppType(normalizedScheme.type))
    }


    for (buildError in buildErrors) {
        if (buildError.value.isNotEmpty()) {
            buildError.value.forEach { error ->
                errorCount += 1

                val sourceName =
                        buildError.key

                log.error("${ppSourceName(sourceName)}:")

                val errorMessage =
                        when (error) {
                            is SyntaxError ->
                                "Syntax Error: ${error.location}: ${error.msg}"

                            is UnboundVariable ->
                                "Unbound Parameter: ${error.location}: ${error.name}"

                            is DuplicateLetDeclaration ->
                                "Duplicate Let Declaration: ${error.location}: ${error.name}"

                            is DuplicateLetSignature ->
                                "Duplicate Let Signature: ${error.location}: ${error.otherLocation}: ${error.name}"

                            is UnificationFail ->
                                "Unification Fail: ${ppType(error.t1)} ${ppType(error.t2)}"

                            is UnificationMismatch ->
                                "Unification Mismatch: ${error.t1s.map { ppType(it) }}: ${error.t2s.map { ppType(it) }}"

                            is DifferingRecordSize ->
                                "Record Unification Fail: ${ppType(error.t1)}: ${ppType(error.t2)}"

                            is RecordFieldNamesMismatch ->
                                "Record Unification Fail: ${error.t1}: ${error.t2}"

                            is UnknownTypeReference ->
                                "Unknown Type Reference: ${error.location}: ${error.name}"

                            is UnknownConstructorReference ->
                                "Unknown Constructor Reference: ${error.location}: ${error.name}"

                            is DuplicateTypeDeclaration ->
                                "Duplicate Type Declaration: ${error.location}: ${error.name}"

                            is DuplicateTypeAliasDeclaration ->
                                "Duplicate Type Alias Declaration: ${error.location}: ${error.name}"

                            is DuplicateConstructorDeclaration ->
                                "Duplicate Constructor Declaration: ${error.location}: ${error.name}"

                            is LetSignatureWithoutDeclaration ->
                                "Let Signature Without Declaration: ${error.location}: ${error.name}"

                            is IncorrectNumberOfSchemeArguments ->
                                "Incorrect Number of Scheme Arguments: ${error.location}: ${error.name}: actual ${error.actual}: expected ${error.expected}"

                            is IncorrectNumberOfAliasArguments ->
                                "Incorrect Number of Alias Arguments: ${error.location}: ${error.name}: actual ${error.actual}: expected ${error.expected}"

                            is IncorrectNumberOfConstructorArguments ->
                                "Incorrect Number of Constructor Arguments: ${error.location}: ${error.name}: actual ${error.actual}: expected ${error.expected}"

                            is TooManyTupleArguments ->
                                "Too Many Tuple Arguments: ${error.location}: actual ${error.actual}: maximum ${error.maximum}"

                            is IncompatibleDeclarationSignature ->
                                "Incompatible Declaration Signature: ${error.location}: ${error.name}: inferred ${ppScheme(error.inferred)}: expected ${ppScheme(error.expected)}"

                            is DeclarationSignatureHasOpenRecord ->
                                "Incompatible Declaration Signature: ${error.location}: ${error.name}: ${ppScheme(error.scheme)}"

                            is OperatorTypeIsOpenRecord ->
                                "Operator Type Is Open Record: ${error.location}: ${ppType(error.type)}"

                            is UnknownType ->
                                "Unknown Type: ${error.location}: ${error.name}"

                            is NonExhaustivePattern ->
                                "Non Exhaustive Pattern: ${error.location}"

                            is ValueNotExported ->
                                "Value not Exported: ${error.location}: ${error.name}"

                            is TypeNotExported ->
                                "Type not Exported: ${error.location}: ${error.name}"

                            is TypeAliasHasNoConstructors ->
                                "Type Alias has no Constructors: ${error.location}: ${error.name}"

                            is ADTHasNoConstructors ->
                                "ADT has no Constructors: ${error.location}: ${error.name}"

                            is TypeConstructorNotExported ->
                                "Type Constructor not Exported: ${error.location}: ${error.name}"

                            is ImportErrors ->
                                "Import Errors: ${error.location}:\n${error.errors}"

                            is UnableToReadFile ->
                                "Unable to read file: ${error.file}"

                            is CyclicDependency ->
                                "Cyclic Dependency: ${error.file}"

                            is DuplicateImportedTypeDeclaration ->
                                "Duplicate Imported Type Name: ${error.location}: ${error.name}"

                            is DuplicateImportedTypeAliasDeclaration ->
                                "Duplicate Imported Type Alias Name: ${error.location}: ${error.name}"

                            is DuplicateImportedLetDeclaration ->
                                "Duplicate Imported Value Name: ${error.location}: ${error.name}"

                            is DuplicateImportedName ->
                                "Duplicate Imported Name: ${error.location}: ${error.name}"

                            is IOException ->
                                "IO Exception: ${error.e.message}: ${error.e.stackTrace}"
                        }

                log.error("  $errorMessage")
            }
        }
    }

    return errorCount
}