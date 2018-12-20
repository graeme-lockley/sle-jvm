package za.co.no9.sle.tools.make

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody


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

    val sources by parser
            .positionalList("SOURCE", help = "source files and directories")
            .default(listOf("."))
}


fun main(args: Array<String>) =
        mainBody {
            val parsedArgs = ArgParser(args).parseInto(::Arguments)

            parsedArgs.run {
                println("Make: Hello World")
                println("  verbose: $verbose")
                println("  sources: $sources")
                println("  target: $target")
            }
        }