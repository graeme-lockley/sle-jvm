package za.co.no9.sle.typing


data class Scheme(val parameters: List<Var>, val type: Type) {
    fun apply(s: Substitution): Scheme =
            Scheme(parameters, type.apply(s - parameters))


    override fun toString(): String =
            "<${parameters.joinToString(", ") { it.toString() }}> $type"


    fun instantiate(varPump: VarPump): Type {
        val asP =
                parameters.map { varPump.fresh(type.location) }

        val substitution =
                Substitution(parameters.zip(asP).toMap())

        return type.apply(substitution)
    }

    fun normalize(): Scheme {
        val subs =
                Substitution(parameters.foldIndexed(emptyMap()) { a, b, c -> b.plus(Pair(c, TVar(type.location, a))) })

        return Scheme(
                parameters.mapIndexed { index, _ -> index },
                type.apply(subs)
        )
    }

    fun isCompatibleWith(environment: Environment, other: Scheme): Boolean {
        val normalizedThis =
                this.normalize()

        val normalizedOther =
                other.normalize()

        return za.co.no9.sle.typing.isCompatibleWith(environment, normalizedThis.type, normalizedOther.type)
    }
}


fun isCompatibleWith(environment: Environment, t1: Type, t2: Type): Boolean =
        when {
            t2 is TVar ->
                if (t1 is TVar)
                    t1.variable == t2.variable
                else
                    true

            t1 is TCon && t2 is TCon ->
                t1.name == t2.name && t1.arguments.size == t2.arguments.size && t1.arguments.zip(t2.arguments).fold(true) { a, b ->
                    a && isCompatibleWith(environment, b.first, b.second)
                }

            t1 is TArr && t2 is TArr ->
                isCompatibleWith(environment, t1.domain, t2.domain) && isCompatibleWith(environment, t1.range, t2.range)

            t1 is TRec && t2 is TRec ->
                if (t1.fields == t2.fields) {
                    true
                } else {
                    val t2Map =
                            t2.fields.toMap()

                    t1.fields.fold(true) { a, b ->
                        a && t2Map.containsKey(b.first) && isCompatibleWith(environment, b.second, t2Map[b.first]!!)
                    }
                }

            t1 is TAlias -> {
                val x =
                        environment.alias(t1.name)

                x != null && isCompatibleWith(environment, x.type, t2)
            }

            t2 is TAlias -> {
                val x =
                        environment.alias(t2.name)

                x != null && isCompatibleWith(environment, t1, x.type)
            }

            else ->
                false
        }


fun generalise(type: Type, substitution: Substitution = nullSubstitution): Scheme {
    val typeFtv =
            type.ftv().toList()

    val substitutionParameters =
            typeFtv.map { TVar(type.location, it).apply(substitution) }

    val typeSubstitution =
            typeFtv.zip(substitutionParameters).map { Substitution(it.first, it.second) }.fold(nullSubstitution) { s, m -> s + m }

    val type1 = type.apply(typeSubstitution)

    return Scheme(type1.ftv().toList(), type1)
}


val errorScheme =
        Scheme(emptyList(), typeError)