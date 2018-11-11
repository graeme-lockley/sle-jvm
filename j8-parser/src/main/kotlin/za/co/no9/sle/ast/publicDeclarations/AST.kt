package za.co.no9.sle.ast.publicDeclarations

import za.co.no9.sle.typing.Scheme


data class Public(val declarations: List<Declaration>)


sealed class Declaration(open val name: String)

data class Value(
        override val name: String,
        val scheme: Scheme) : Declaration(name)