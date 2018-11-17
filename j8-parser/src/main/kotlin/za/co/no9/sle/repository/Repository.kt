package za.co.no9.sle.repository

import za.co.no9.sle.Either
import za.co.no9.sle.Errors
import java.io.File


interface Repository {
    fun import(name: String): Either<Errors, Export>

    fun export(source: Source, inputFile: File, value: Export): Errors
}



enum class Source {
    File, Github
}