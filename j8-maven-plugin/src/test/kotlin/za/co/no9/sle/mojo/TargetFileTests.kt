package za.co.no9.sle.mojo

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.repository.Source
import java.io.File

class TargetFileTests : StringSpec({
    val repository =
            Repository(File("/home/gjl/src"), File("/home/gjl/.sle"))

    "sourcePrefix: '/home/gjl/src', targetRoot: '/home/gjl/.sle', source: File, input: '/home/gjl/src/List.sle' 'json' maps into /home/gjl/.sle/file/List.(json|java)" {
        val item =
                repository.item(Source.File, File("/home/gjl/src/List.sle"))

        item.targetJavaFile().absolutePath
                .shouldBe("/home/gjl/.sle/file/List.java")

        item.targetJsonFile().absolutePath
                .shouldBe("/home/gjl/.sle/file/List.json")
    }

    "sourcePrefix: '/home/gjl/src', targetRoot: '/home/gjl/.sle', source: File, input: '/home/gjl/tmp/List.sle' maps into /home/gjl/.sle/file/home/gjl/tmp/List.(json|java)" {
        val item =
                repository.item(Source.File, File("/home/gjl/tmp/List.sle"))

        item.targetJavaFile().absolutePath
                .shouldBe("/home/gjl/.sle/file/home/gjl/tmp/List.java")

        item.targetJsonFile().absolutePath
                .shouldBe("/home/gjl/.sle/file/home/gjl/tmp/List.json")
    }

    "sourcePrefix: '/home/gjl/src', targetRoot: '/home/gjl/.sle', source: File, input: '/home/gjl/src/List.sle' item ['file'].List" {
        val item =
                repository.item(Source.File, File("/home/gjl/src/List.sle"))

        item.packageName
                .shouldBe(listOf("file"))

        item.className
                .shouldBe("List")
    }

    "sourcePrefix: '/home/gjl/src', targetRoot: '/home/gjl/.sle', source: File, input: '/home/gjl/tmp/List.sle' item ['file', 'home', 'gjl', 'tmp'].List" {
        val item =
                repository.item(Source.File, File("/home/gjl/tmp/List.sle"))

        item.packageName
                .shouldBe(listOf("file", "home", "gjl", "tmp"))

        item.className
                .shouldBe("List")
    }
})
