package za.co.no9.sle


data class QString(private val qualifier: String?, private val string: String) {
    override fun toString(): String =
            if (qualifier == null)
                string
            else
                "$qualifier.$string"
}