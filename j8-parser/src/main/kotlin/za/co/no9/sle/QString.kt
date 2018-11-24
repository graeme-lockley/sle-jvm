package za.co.no9.sle


data class QString(val qualifier: String?, val string: String) {
    override fun toString(): String =
            if (qualifier == null)
                string
            else
                "$qualifier.$string"
}