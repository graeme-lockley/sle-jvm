package za.co.no9.sle.transform.parseTreeToTypeless


class Stack<T>(private var stack: List<T> = emptyList()) {
    fun pop(): T {
        val result =
                stack.last()

        stack = stack.dropLast(1)

        return result
    }


    fun popN(n: Int): List<T> {
        var items =
                emptyList<T>()

        for (lp in 1..n) {
            items += pop()
        }

        return items
    }


    fun popNReversed(n: Int): List<T> =
            popN(n).asReversed()


    fun popAllReversed(): List<T> {
        val result =
                stack

        stack = emptyList()

        return result
    }


    fun push(item: T) {
        stack += item
    }
}