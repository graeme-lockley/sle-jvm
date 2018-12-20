package za.co.no9.sle.tools.make

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import za.co.no9.sle.tools.build.build
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
                    throw InvalidArgumentException("Target directory is not writables: $value")
                }
            }


    val source by parser
            .storing("-S", "--source", help = "source directory of files to compile")
            .default(File(".").absolutePath)
}


fun main(args: Array<String>) =
        mainBody {
            val parsedArgs = ArgParser(args).parseInto(::Arguments)

            parsedArgs.run {
                val log =
                        Log()

                if (verbose) {
                    log.info("source: $source")
                    log.info("target: $target")
                }
                build(Log(), File(source), File(target))
            }
        }


class Log : za.co.no9.sle.tools.build.Log {
    override fun error(message: String) {
        println("Error: $message")
    }

    override fun info(message: String) {
        println("Info: $message")
    }
}