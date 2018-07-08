package za.co.no9.sle

fun <X, Y, Z> Pair<X, Y>.mapFirst(f: (X) -> Z): Pair<Z, Y> =
        Pair(f(this.first), this.second)

fun <X, Y, Z> Pair<X, Y>.mapSecond(f: (Y) -> Z): Pair<X, Z> =
        Pair(this.first, f(this.second))


