package za.co.no9.sle.mojo

import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.logging.Log
import za.co.no9.sle.*
import za.co.no9.sle.parser.parseModule
import za.co.no9.sle.pass1.parseTreeToAST
import za.co.no9.sle.pass2.astToCoreAST
import za.co.no9.sle.pass3.assignTypesToCoreAST
import za.co.no9.sle.pass4.translateToJava
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



    sourceFile.walk().filter { it.isFile }.filter { it.absolutePath.endsWith(".sle") }.forEach {
        val targetFileName =
                File(targetFile, it.absolutePath.drop(sourceFile.absolutePath.length).dropLast(4) + ".java")

        if (!targetFileName.exists() || targetFileName.isFile && targetFileName.lastModified() < it.lastModified()) {
            val sourceName =
                    it.name

            val environment =
                    Environment(mapOf(
                            Pair("(==)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeBool)))),
                            Pair("(-)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))),
                            Pair("(+)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt)))),
                            Pair("(*)", Schema(listOf(), TArr(typeInt, TArr(typeInt, typeInt))))))

            val packageName =
                    it.parentFile.absolutePath.drop(sourceFile.absolutePath.length + 1).replace(File.separatorChar, '.', true)

            val className =
                    it.nameWithoutExtension

            val output =
                    parseModule(it.readText())
                            .map { parseTreeToAST(it.node) }
                            .map { astToCoreAST(it) }
                            .andThen { it.assignTypesToCoreAST(environment) }
                            .map { translateToJava(it, packageName, className) }
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
                            log.error("Unbound Variable: $sourceName: ${it.location}: ${it.name}")

                        is DuplicateLetDeclaration ->
                            log.error("Duplicate Let Declaration: $sourceName: ${it.location}: ${it.name}")

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
