package za.co.no9.sle.tools.build

import za.co.no9.sle.Either
import za.co.no9.sle.Errors
import za.co.no9.sle.URN
import za.co.no9.sle.value
import java.io.File


abstract class Repository(
        open val sourcePrefix: File,
        open val targetRoot: File) : za.co.no9.sle.repository.Repository<Item> {
    override fun item(urn: URN): Either<Errors, Item> {
        val item =
                Item(this, urn)

        itemLoaded(item)

        return value(item)
    }


    abstract fun itemLoaded(item: Item)


    fun sourceFiles() =
            sourcePrefix.walk().filter { it.isFile }.filter { it.name.endsWith(".sle") }.map { URN(it) }.toSet()
}
