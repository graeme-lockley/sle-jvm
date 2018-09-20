package za.co.no9.sle.mojo

import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.logging.Log
import za.co.no9.sle.*
import za.co.no9.sle.transform.typelessCoreToTypedCore.parseWithDetail
import za.co.no9.sle.pass4.translateToJava
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

                val sourceFileName =
                        it.first

                val sourceName =
                        sourceFileName.name

                val environment =
                        Environment(mapOf(
                                Pair("(==)", Schema(listOf(1), TArr(TVar(1), TArr(TVar(1), typeBool)))),
                                Pair("(!=)", Schema(listOf(1), TArr(TVar(1), TArr(TVar(1), typeBool)))),
                                Pair("(<)", Schema(listOf(1), TArr(TVar(1), TArr(TVar(1), typeBool)))),
                                Pair("(<=)", Schema(listOf(1), TArr(TVar(1), TArr(TVar(1), typeBool)))),
                                Pair("(>)", Schema(listOf(1), TArr(TVar(1), TArr(TVar(1), typeBool)))),
                                Pair("(>=)", Schema(listOf(1), TArr(TVar(1), TArr(TVar(1), typeBool)))),
                                Pair("(&&)", Schema(listOf(), TArr(typeBool, TArr(typeBool, typeBool)))),
                                Pair("(||)", Schema(listOf(), TArr(typeBool, TArr(typeBool, typeBool)))),
                                Pair("(-)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))),
                                Pair("(+)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))),
                                Pair("(*)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))),
                                Pair("(/)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))))

                val packageName =
                        sourceFileName.parentFile.absolutePath.drop(sourceFile.absolutePath.length + 1).replace(File.separatorChar, '.', true)

                val className =
                        sourceFileName.nameWithoutExtension

                val output =
                        parseWithDetail(sourceFileName.readText(), environment)
                                .map { translateToJava(it.resolvedModule, packageName, className) }
                                .map { it.toString() }

                val errors =
                        output.left()

                if (errors == null) {
                    targetFileName.parentFile.mkdirs()
                    targetFileName.writeText(output.right() ?: "")
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
                        }
                    }

                    throw MojoFailureException(sourceFile, "Translation Error", "${errors.size} errors")
                }
            }
        }
    }
}
