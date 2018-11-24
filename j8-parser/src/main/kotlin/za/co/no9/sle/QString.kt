package za.co.no9.sle


data class QString(val qualifier: String?, val string: String) {
    constructor(string: String) : this(null, string)

    override fun toString(): String =
            if (qualifier == null)
                string
            else
                "$qualifier.$string"
}