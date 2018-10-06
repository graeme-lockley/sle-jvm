package za.co.no9.sle.transform.parseTreeToTypeless


class Stack<T>(private val stack: MutableList<T> = mutableListOf()) {
    fun pop(): T =
            stack.removeAt(stack.size - 1)


    private fun popN(n: Int): List<T> {
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
                stack.toList()

        stack.clear()

        return result
    }


    fun push(item: T) =
            stack.add(stack.size, item)
}