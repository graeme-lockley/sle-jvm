package za.co.no9.sle.pass4

import za.co.no9.sle.ast.core.*
import za.co.no9.sle.repository.*
import za.co.no9.sle.repository.Constructor
import za.co.no9.sle.repository.Declaration
import za.co.no9.sle.repository.LetDeclaration
import za.co.no9.sle.typing.Environment
import za.co.no9.sle.typing.resolveAlias


fun toClass(environment: Environment, source: Item, nameDeclarations: List<NameDeclaration>): Export =
        Export(nameDeclarations.map { toClass(environment, source, it) })


private fun toClass(environment: Environment, source: Item, nameDeclaration: NameDeclaration): Declaration =
        when (nameDeclaration) {
            is ValueNameDeclaration ->
                LetDeclaration(nameDeclaration.name, toClass(environment, nameDeclaration.scheme))

            is OperatorNameDeclaration ->
                OperatorDeclaration(nameDeclaration.name, toClass(environment, nameDeclaration.scheme), nameDeclaration.precedence, nameDeclaration.associativity.asString())

            is AliasNameDeclaration ->
                AliasDeclaration(nameDeclaration.name, toClass(environment, nameDeclaration.scheme))

            is ADTNameDeclaration ->
                OpaqueADTDeclaration(nameDeclaration.name, nameDeclaration.scheme.parameters.size, source.resolveConstructor(nameDeclaration.name))

            is FullADTNameDeclaration ->
                ADTDeclaration(nameDeclaration.name, nameDeclaration.scheme.parameters.size, source.resolveConstructor(nameDeclaration.name), nameDeclaration.constructors.map { Constructor(it.name, toClass(environment, it.scheme)) })
        }


private fun toClass(environment: Environment, scheme: za.co.no9.sle.typing.Scheme): Scheme =
        Scheme(scheme.parameters, toClass(environment, scheme.type))


private fun toClass(environment: Environment, type: za.co.no9.sle.typing.Type): Type =
        when (type) {
            is za.co.no9.sle.typing.TVar ->
                Variable(type.variable)

            is za.co.no9.sle.typing.TAlias ->
                toClass(environment, resolveAlias(environment, type)!!)

            is za.co.no9.sle.typing.TCon ->
                Constant(type.name, type.arguments.map { toClass(environment, it) })

            is za.co.no9.sle.typing.TArr ->
                Arrow(toClass(environment, type.domain), toClass(environment, type.range))

            is za.co.no9.sle.typing.TRec ->
                Record(type.fixed, type.fields.map { Field(it.first, toClass(environment, it.second)) })
        }

