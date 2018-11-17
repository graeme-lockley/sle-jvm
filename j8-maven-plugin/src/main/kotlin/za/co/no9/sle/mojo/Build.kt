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

    val sources: Sequence<File> =
            sourceFile.walk().filter { it.isFile }.filter { it.absolutePath.endsWith(".sle") }

    if (sources.count() == 0) {
        log.warn("No sources to compile")
    } else {
        val toTranslate =
                sources.map {
                    Pair(it, File(targetFile, it.absolutePath.drop(sourceFile.absolutePath.length).dropLast(4) + ".java"))
                }.filter { !it.second.exists() || it.second.isFile && it.second.lastModified() < it.first.lastModified() }

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
                val targetFileName =
                        it.second

                val exportFileName =
                        File(it.second.absolutePath.dropLast(4) + "json")

                val sourceFileName =
                        it.first

                val sourceName =
                        sourceFileName.name

                val environment =
                        initialEnvironment
                                .newValue("(==)", Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool))))
                                .newValue("(!=)", Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool))))
                                .newValue("(<)", Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool))))
                                .newValue("(<=)", Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool))))
                                .newValue("(>)", Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool))))
                                .newValue("(>=)", Scheme(listOf(1), TArr(TVar(homeLocation, 1), TArr(TVar(homeLocation, 1), typeBool))))
                                .newValue("(&&)", Scheme(listOf(), TArr(typeBool, TArr(typeBool, typeBool))))
                                .newValue("(||)", Scheme(listOf(), TArr(typeBool, TArr(typeBool, typeBool))))
                                .newValue("(-)", Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))
                                .newValue("(+)", Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))
                                .newValue("(*)", Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))
                                .newValue("(/)", Scheme(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))


                val packageName =
                        sourceFileName.parentFile.absolutePath.drop(sourceFile.absolutePath.length + 1).replace(File.separatorChar, '.', true)

                val className =
                        sourceFileName.nameWithoutExtension

                val parseDetail =
                        parseWithDetail(sourceFileName.readText(), environment)

                val compiledFile =
                        parseDetail
                                .map { translateToJava(it.coreModule, packageName, className) }

                val output =
                        compiledFile
                                .map { it.toString() }

                val errors =
                        output.left()

                if (errors == null) {
                    targetFileName.parentFile.mkdirs()
                    targetFileName.writeText(output.right() ?: "")
                    exportFileName.writeText(toJsonString(toClass(parseDetail.right()!!.coreModule.exports)))
                } else {
                    errors.forEach {
                        when (it) {
                            is SyntaxError ->
                                log.error("Syntax Error: $sourceName: ${it.location}: ${it.msg}")

                            is UnboundVariable ->
                                log.error("Unbound Parameter: $sourceName: ${it.location}: ${it.name}")

                            is DuplicateLetDeclaration ->
                                log.error("Duplicate Let Declaration: $sourceName: ${it.location}: ${it.name}")

                            is DuplicateLetSignature ->
                                log.error("Duplicate Let Signature: $sourceName: ${it.location}: ${it.otherLocation}: ${it.name}")

                            is UnificationFail ->
                                log.error("Unification Fail: $sourceName: ${it.t1} ${it.t2}")

                            is UnificationMismatch ->
                                log.error("Unification Mismatch: $sourceName: ${it.t1s}: ${it.t2s}")

                            is UnknownTypeReference ->
                                log.error("Unknown Type Reference: $sourceName: ${it.location}: ${it.name}")

                            is UnknownConstructorReference ->
                                log.error("Unknown Constructor Reference: $sourceName: ${it.location}: ${it.name}")

                            is DuplicateTypeDeclaration ->
                                log.error("Duplicate Type Declaration: $sourceName: ${it.location}: ${it.name}")

                            is DuplicateTypeAliasDeclaration ->
                                log.error("Duplicate Type Alias Declaration: $sourceName: ${it.location}: ${it.name}")

                            is DuplicateConstructorDeclaration ->
                                log.error("Duplicate Constructor Declaration: $sourceName: ${it.location}: ${it.name}")

                            is LetSignatureWithoutDeclaration ->
                                log.error("Let Signature Without Declaration: $sourceName: ${it.location}: ${it.name}")

                            is IncorrectNumberOfSchemeArguments ->
                                log.error("Incorrect Number of Scheme Arguments: $sourceName: ${it.location}: ${it.name}: actual ${it.actual}: expected ${it.expected}")

                            is IncorrectNumberOfAliasArguments ->
                                log.error("Incorrect Number of Alias Arguments: $sourceName: ${it.location}: ${it.name}: actual ${it.actual}: expected ${it.expected}")

                            is IncorrectNumberOfConstructorArguments ->
                                log.error("Incorrect Number of Constructor Arguments: $sourceName: ${it.location}: ${it.name}: actual ${it.actual}: expected ${it.expected}")

                            is IncompatibleDeclarationSignature ->
                                log.error("Incompatible Declaration Signature: $sourceName: ${it.location}: ${it.name}: inferred ${it.inferred}: expected ${it.expected}")

                            is UnknownType ->
                                log.error("Unknown Type: $sourceName: ${it.location}: ${it.name}")

                            is NonExhaustivePattern ->
                                log.error("Non Exhaustive Pattern: $sourceName: ${it.location}")

                            else ->
                                log.error("Unknown Error: $it")
                        }
                    }

                    throw MojoFailureException(sourceFile, "Translation Error", "${errors.size} errors")
                }
            }
        }
    }
}
