package za.co.no9.sle.typing


class VarPump {
    private var counter =
            0

    fun fresh(): TVar {
        val result =
                counter

        counter += 1

        return TVar(result)
    }
}