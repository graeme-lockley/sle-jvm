package za.co.no9.sle.pass4

import com.google.gson.Gson
import io.kotlintest.specs.StringSpec



data class ARecord (val name: String, val items: List<String>)

class SnakeYAMLTests : StringSpec({
    "create YAML record" {
        val gson =
                Gson()

        val aRecord = ARecord("Hello", listOf("item1", "item2", "item3"))


        val s =
                gson.toJson(aRecord)!!

        val bRecord = gson.fromJson(s, ARecord::class.java)

        println(aRecord)
        println(s)
        println(bRecord)
    }
})