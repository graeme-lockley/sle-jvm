package za.co.no9.sle.pass4

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import za.co.no9.sle.homeLocation
import za.co.no9.sle.typing.*


class JavaTypeTests : StringSpec({
    "Int" {
        javaType(typeInt).shouldBe("Object")
    }

    "String" {
        javaType(typeString).shouldBe("Object")
    }

    "Bool" {
        javaType(typeBool).shouldBe("Object")
    }

    "Bool -> String" {
        javaType(TArr(typeBool, typeString)).shouldBe("Function<Object, Object>")
    }

    "Bool -> '10" {
        javaType(TArr(typeBool, TVar(homeLocation, 10))).shouldBe("Function<Object, Object>")
    }
})