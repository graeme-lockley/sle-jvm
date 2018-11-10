package za.co.no9.sle

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class URNTests : StringSpec({
    "github:Data.List:1.2.1" {
        URN("github:Data.List:1.2.1")
                .shouldBe(URN("github", "Data.List", "1.2.1"))
    }

    "github:Data.List" {
        URN("github:Data.List")
                .shouldBe(URN("github", "Data.List", null))
    }

    "file:../Data/File" {
        URN("file:../Data/File")
                .shouldBe(URN("file", "../Data/File", null))
    }
})
