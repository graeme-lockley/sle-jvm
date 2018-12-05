package za.co.no9.sle.mojo

import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.logging.Log
import za.co.no9.sle.*
import za.co.no9.sle.pass4.toClass
import za.co.no9.sle.pass4.translateToJava
import za.co.no9.sle.repository.toJsonString
import za.co.no9.sle.transform.enrichedCoreToCore.parseWithDetail
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
            Repository(sourceFile, targetFile)

    val sources: Sequence<File> =
            sourceFile.walk().filter { it.isFile }.filter { it.absolutePath.endsWith(".sle") }

    if (sources.count() == 0) {
        log.warn("No sources to compile")
    } else {
        val toTranslate =
                sources.map {
                    Pair(it, repository.item(Source.File, it))
                }.filter { it.second.mustCompile() }

        if (toTranslate.count() == 0) {
            log.info("Sources up to date")
        } else {
            val translateCount =
                    toTranslate.count()

            if (translateCount == 1) {
                log.info("Compiling $translateCount source file")
            } else {
                log.info("Compiling $translateCount source files")
            }

            toTranslate.forEach {
                val item =
                        it.second


                val packageName =
                        item.packageName.joinToString(".")

                val className =
                        item.className

                val parseDetail =
                        parseWithDetail(item, environment)

                val compiledFile =
                        parseDetail
                                .map { translateToJava(it.coreModule, packageName, className) }

                val output =
                        compiledFile
                                .map { it.toString() }

                val errors =
                        output.left()

                if (errors == null) {
                    item.writeJava(output.right() ?: "")
                    item.writeJson(toJsonString(toClass(item, parseDetail.right()!!.coreModule.exports)))
                } else {
                    val sourceName =
                            it.second.sourceFile().absoluteFile

                    errors.forEach { error ->
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
                                }

                        log.error(errorMessage)
                    }

                    throw MojoFailureException(sourceFile, "Translation Error", "${errors.size} errors")
                }
            }
        }
    }
}


private val environment =
        initialEnvironment
                .newValue("(==)", VariableBinding(Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool)))))
                .newValue("(!=)", VariableBinding(Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool)))))
                .newValue("(<)", VariableBinding(Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool)))))
                .newValue("(<=)", VariableBinding(Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool)))))
                .newValue("(>)", VariableBinding(Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool)))))
                .newValue("(>=)", VariableBinding(Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool)))))
                .newValue("(&&)", VariableBinding(Scheme(listOf(), TArr(typeBool, TArr(typeBool, typeBool)))))
                .newValue("(||)", VariableBinding(Scheme(listOf(), TArr(typeBool, TArr(typeBool, typeBool)))))
                .newValue("(-)", VariableBinding(Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))))
                .newValue("(+)", VariableBinding(Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))))
                .newValue("(*)", VariableBinding(Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))))
                .newValue("(/)", VariableBinding(Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))))

