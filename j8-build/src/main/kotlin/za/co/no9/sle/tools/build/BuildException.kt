package za.co.no9.sle.tools.build

import java.io.File

class BuildException(val source: File, shortMessage: String, val longMessage: String) : Exception(shortMessage)