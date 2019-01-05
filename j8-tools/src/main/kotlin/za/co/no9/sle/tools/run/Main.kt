package za.co.no9.sle.tools.run

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import za.co.no9.sle.URN
import za.co.no9.sle.right
import za.co.no9.sle.runtime.ActorUtil
import za.co.no9.sle.tools.build.Item
import java.io.File
import java.util.function.Function


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

    val sle by parser
            .positional("SLE file to run")

    val args by parser
            .positionalList("ARGS", "arguments to be passed to main")
            .default(emptyList())
}


private val Nil: Array<Any> = arrayOf(0)

private fun Cons(x: String, xs: Array<Any>): Array<Any> =
        arrayOf(1, x, xs)


fun main(arguments: Array<String>) =
        mainBody {
            val parsedArgs = ArgParser(arguments).parseInto(::Arguments)

            parsedArgs.run {
                val log =
                        Log()

                if (verbose) {
                    log.info("source: $source")
                    log.info("target: $target")
                }

                val repository =
                        Repository(File(source), File(target))

                val item =
                        repository.item(URN(File(sle)))

                val rightItem =
                        item.right()

                if (rightItem != null) {
                    val className =
                            rightItem.packageName.joinToString(".") + "." + rightItem.className

                    val clazz =
                            Class.forName(className)

                    val field =
                            clazz.getField("main")

                    val value =
                            field.get(null)

                    if (value is java.util.function.Function<*, *>) {
                        @Suppress("UNCHECKED_CAST")

                        printValue((value as Function<Any, Any>).apply(args.foldRight(Nil) { a, b -> Cons(a, b) }))
                    } else {
                        printValue(value)
                    }
                    println()
                }
            }
            (ActorUtil.synchronousWait as java.util.function.Function<Int, Any>).apply(10)
            System.exit(0)
        }


fun printValue(value: Any?) {
    if (value is Array<*>) {
        print('[')
        for (lp in 0 until value.size) {
            if (lp > 0)
                print(", ")

            printValue(value[lp])
        }
        print("]")
    } else {
        print(value)
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


class Repository(override val sourcePrefix: File,
                 override val targetRoot: File) : za.co.no9.sle.tools.build.Repository(sourcePrefix, targetRoot) {
    override fun itemLoaded(item: Item) {
    }
}