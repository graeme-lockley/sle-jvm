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


