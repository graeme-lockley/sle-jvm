package za.co.no9.sle.repository

import za.co.no9.sle.Either
import za.co.no9.sle.Errors
import za.co.no9.sle.URN


interface Repository<out T : Item> {
    fun item(urn: URN): Either<Errors, T>
}


interface Item {
    fun exports(): Export

    fun sourceCode(): String

    fun sourceURN(): URN

    fun resolveConstructor(name: String): String

    fun resolveId(name: String): String

    fun itemRelativeTo(name: String): Either<Errors, Item>
}
