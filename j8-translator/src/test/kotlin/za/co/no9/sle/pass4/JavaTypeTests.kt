package za.co.no9.sle.pass4

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.typing.*


class JavaTypeTests : StringSpec({
    "Int" {
        javaType(typeInt).shouldBe("Integer")
    }

    "String" {
        javaType(typeString).shouldBe("String")
    }

    "Bool" {
        javaType(typeBool).shouldBe("Boolean")
    }

    "Bool -> String" {
        javaType(TArr(typeBool, typeString)).shouldBe("Function<Boolean, String>")
    }

    "!'10" {
        javaType(TVar(10)).shouldBe("Function<Boolean, String>")
    }
})