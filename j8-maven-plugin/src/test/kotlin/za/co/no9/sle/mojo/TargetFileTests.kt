package za.co.no9.sle.mojo

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.URN
import za.co.no9.sle.right
import java.io.File

class TargetFileTests : StringSpec({
    val repository =
            TestRepository(File("/home/gjl/src"), File("/home/gjl/.sle"))

    "sourcePrefix: '/home/gjl/src', targetRoot: '/home/gjl/.sle', source: File, input: '/home/gjl/src/List.sle' 'json' maps into /home/gjl/.sle/file/List.(json|java)" {
        val item =
                repository.item(URN(za.co.no9.sle.File, File("/home/gjl/src/List.sle"))).right()!!

        item.targetJavaFile().absolutePath
                .shouldBe("/home/gjl/.sle/file/List.java")

        item.targetJsonFile().absolutePath
                .shouldBe("/home/gjl/.sle/file/List.json")
    }

    "sourcePrefix: '/home/gjl/src', targetRoot: '/home/gjl/.sle', source: File, input: '/home/gjl/tmp/List.sle' maps into /home/gjl/.sle/file/home/gjl/tmp/List.(json|java)" {
        val item =
                repository.item(URN(za.co.no9.sle.File, File("/home/gjl/tmp/List.sle"))).right()!!

        item.targetJavaFile().absolutePath
                .shouldBe("/home/gjl/.sle/file/home/gjl/tmp/List.java")

        item.targetJsonFile().absolutePath
                .shouldBe("/home/gjl/.sle/file/home/gjl/tmp/List.json")
    }

    "sourcePrefix: '/home/gjl/src', targetRoot: '/home/gjl/.sle', source: File, input: '/home/gjl/src/List.sle' item ['file'].List" {
        val item =
                repository.item(URN(za.co.no9.sle.File, File("/home/gjl/src/List.sle"))).right()!!

        item.packageName
                .shouldBe(listOf("file"))

        item.className
                .shouldBe("List")
    }

    "sourcePrefix: '/home/gjl/src', targetRoot: '/home/gjl/.sle', source: File, input: '/home/gjl/tmp/List.sle' item ['file', 'home', 'gjl', 'tmp'].List" {
        val item =
                repository.item(URN(za.co.no9.sle.File, File("/home/gjl/tmp/List.sle"))).right()!!

        item.packageName
                .shouldBe(listOf("file", "home", "gjl", "tmp"))

        item.className
                .shouldBe("List")
    }

    "sourcePrefix: '/home/gjl/src', targetRoot: '/home/gjl/.sle', source: File, input: '/home/gjl/src/List.sle' resolveConstructor('Cons')" {
        val item =
                repository.item(URN(za.co.no9.sle.File, File("/home/gjl/src/List.sle"))).right()!!

        item.resolveConstructor("Cons")
                .shouldBe("file.List.Cons")
    }

    "sourcePrefix: '/home/gjl/src', targetRoot: '/home/gjl/.sle', source: File, input: '/home/gjl/src/Data/List.sle' resolveConstructor('Cons')" {
        val item =
                repository.item(URN(za.co.no9.sle.File, File("/home/gjl/src/Data/List.sle"))).right()!!

        item.resolveConstructor("Cons")
                .shouldBe("file.Data.List.Cons")
    }
})


class TestRepository(override val sourcePrefix: File,
                     override val targetRoot: File) : Repository(sourcePrefix, targetRoot) {

    override fun itemLoaded(item: Item) {}
}