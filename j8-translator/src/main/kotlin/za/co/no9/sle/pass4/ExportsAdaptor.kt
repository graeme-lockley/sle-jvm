package za.co.no9.sle.pass4

import za.co.no9.sle.ast.core.*
import za.co.no9.sle.repository.*
import za.co.no9.sle.repository.Constructor
import za.co.no9.sle.repository.Declaration
import za.co.no9.sle.repository.LetDeclaration


fun toClass(nameDeclarations: List<NameDeclaration>): Export =
        Export(nameDeclarations.map { toClass(it) })


private fun toClass(nameDeclaration: NameDeclaration): Declaration =
        when (nameDeclaration) {
            is ValueNameDeclaration ->
                LetDeclaration(nameDeclaration.name, toClass(nameDeclaration.scheme))

            is AliasNameDeclaration ->
                AliasDeclaration(nameDeclaration.name, toClass(nameDeclaration.scheme))

            is ADTNameDeclaration ->
                ADTDeclaration(nameDeclaration.name, toClass(nameDeclaration.scheme))

            is FullADTNameDeclaration ->
                FullADTDeclaration(nameDeclaration.name, toClass(nameDeclaration.scheme), nameDeclaration.constructors.map { Constructor(it.name, toClass(it.scheme)) })
        }


private fun toClass(scheme: za.co.no9.sle.typing.Scheme): Scheme =
        Scheme(scheme.parameters, toClass(scheme.type))

private fun toClass(type: za.co.no9.sle.typing.Type): Type =
        when (type) {
            is za.co.no9.sle.typing.TVar ->
                Variable(type.variable)

            is za.co.no9.sle.typing.TCon ->
                Constant(type.name, type.arguments.map { toClass(it) })

            is za.co.no9.sle.typing.TArr ->
                Arrow(toClass(type.domain), toClass(type.range))
        }

