package za.co.no9.sle

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class URNTests : StringSpec({
    "github:Data.List:1.2.1 as URN with implied name being List" {
        val urn =
                URN("github:Data.List:1.2.1")

        urn.shouldBe(URN(Github, "Data.List", "1.2.1"))
        urn.impliedName().shouldBe("List")
    }

    "github:Data.List as URN with implied name being List" {
        val urn =
                URN("github:Data.List")

        urn.shouldBe(URN(Github, "Data.List", null))
        urn.impliedName().shouldBe("List")
    }

    "file:../Data/File as URN with implied name being Filer" {
        val urn =
                URN("file:../Data/File")

        urn.shouldBe(URN(File, "../Data/File", null))
        urn.impliedName().shouldBe("File")
    }
})
