package za.co.no9.sle.mojo

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.repository.Source
import java.io.File

class TargetFileTests : StringSpec({
    val repository =
            Repository(File("/home/gjl/src"), File("/home/gjl/.sle"))

    "sourcePrefix: '/home/gjl/src', targetRoot: '/home/gjl/.sle', source: File, input: '/home/gjl/src/List.sle', extension: 'json' maps into /home/gjl/.sle/file/List.json" {
        repository.targetFileName(Source.File, File("/home/gjl/src/List.sle"), "json")
                .absolutePath.shouldBe("/home/gjl/.sle/file/List.json")
    }

    "sourcePrefix: '/home/gjl/src', targetRoot: '/home/gjl/.sle', source: File, input: '/home/gjl/tmp/List.sle', extension: 'json' maps into /home/gjl/.sle/file/home/gjl/tmp/List.json" {
        repository.targetFileName(Source.File, File("/home/gjl/tmp/List.sle"), "json")
                .absolutePath.shouldBe("/home/gjl/.sle/file/home/gjl/tmp/List.json")
    }

    "sourcePrefix: '/home/gjl/src', targetRoot: '/home/gjl/.sle', source: File, input: '/home/gjl/src/List.sle', extension: 'json' export detail ['file'].List" {
        repository.exportDetail(Source.File, File("/home/gjl/src/List.sle"))
                .shouldBe(ExportDetail(listOf("file"), "List"))
    }

    "sourcePrefix: '/home/gjl/src', targetRoot: '/home/gjl/.sle', source: File, input: '/home/gjl/tmp/List.sle', extension: 'json' export detail ['file', 'home', 'gjl', 'tmp'].List" {
        repository.exportDetail(Source.File, File("/home/gjl/tmp/List.sle"))
                .shouldBe(ExportDetail(listOf("file", "home", "gjl", "tmp"), "List"))
    }
})
