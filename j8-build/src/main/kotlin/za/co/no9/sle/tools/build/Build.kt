package za.co.no9.sle.tools.build

import za.co.no9.sle.*
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
                                "Unification Fail: ${error.t1} ${error.t2}"

                            is UnificationMismatch ->
                                "Unification Mismatch: ${error.t1s}: ${error.t2s}"

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
                                "Incompatible Declaration Signature: ${error.location}: ${error.name}: inferred ${error.inferred}: expected ${error.expected}"

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