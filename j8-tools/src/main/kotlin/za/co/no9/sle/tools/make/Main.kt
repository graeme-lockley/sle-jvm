package za.co.no9.sle.tools.make

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import za.co.no9.sle.right
import za.co.no9.sle.tools.build.BuildRepository
import java.io.File


class Arguments(parser: ArgParser) {
    val verbose by parser
            .flagging("enable verbose mode")
            .default(false)

    val target by parser
            .storing("-T", "--target", help = "target directory of compiled code")
            .default(System.getenv("HOME") + "/.sle")
            .addValidator {
                val directory: java.io.File =
                        java.io.File(value)

                if (directory.exists() && directory.isFile) {
                    throw InvalidArgumentException("Target directory is not writable: $value")
                }
            }


    val source by parser
            .storing("-S", "--source", help = "source directory of files to compile")
            .default(File(".").absolutePath)

    val javac by parser
            .storing("--javaccp", help = "javac classpath parameter")
            .default("/Users/graemelockley/Projects/sle-jvm/j8-runtime/target/j8-runtime-1.0-SNAPSHOT.jar")
}


fun main(args: Array<String>): Unit =
        mainBody {
            val parsedArgs =
                    ArgParser(args).parseInto(::Arguments)

            parsedArgs.run {
                val log =
                        Log()

                if (verbose) {
                    log.info("source: $source")
                    log.info("target: $target")
                }

                File(target).mkdirs()

                val repository =
                        BuildRepository(File(source), File(target))

                if (repository.buildErrors.isEmpty()) {
                    println("${repository.compiled.size} files compiled - no errors")

                    val pb = ProcessBuilder(*arrayOf("javac", "-sourcepath", target, "-classpath", javac) + repository.compiled.map { repository.item(it).right()!!.targetJavaFile().absolutePath })
                    pb.redirectErrorStream(true)

                    val p =
                            pb.start()

                    val inputStream =
                            p.inputStream

                    var ch = inputStream.read()
                    while (ch != -1) {
                        if (verbose)
                            print(ch.toChar())

                        ch = inputStream.read()
                    }
                    p.waitFor()

                    val exitWith =
                            p.exitValue()

                    if (verbose || exitWith != 0)
                        println("Error: javac exited with $exitWith")
                } else {
                    println("Errors: ${repository.buildErrors}")
                }
            }
        }


class Log : za.co.no9.sle.tools.build.Log {
    override fun error(message: String) {
        println("Error: $message")
    }

    override fun info(message: String) {
        println(message)
    }
}