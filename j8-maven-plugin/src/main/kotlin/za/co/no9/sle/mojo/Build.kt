package za.co.no9.sle.mojo

import org.apache.maven.plugin.logging.Log

fun build(log: Log, source: String, target: String) {
    log.info("Kotlin: $source  $target")
}
