package za.co.no9.sle.repository

import za.co.no9.sle.Either
import za.co.no9.sle.Errors
import za.co.no9.sle.Source
import java.io.File


interface Repository<out T: Item> {
    fun import(name: String): Either<Errors, Export>

    fun item(source: Source, inputFile: File): T
}


interface Item