package za.co.no9.sle.mojo

import org.apache.maven.plugin.logging.Log
import java.io.File

fun build(log: Log, source: String, target: String) {
    log.info("Kotlin: $source  $target")

    val sourceFile =
            File(source)

    val targetFile =
            File(target)

    if (!sourceFile.isDirectory) {
        log.error("Source $source is not a valid directory")
        return
    }
    if (!targetFile.isDirectory) {
        log.error("Target $target is not a valid directory")
        return
    }
}
