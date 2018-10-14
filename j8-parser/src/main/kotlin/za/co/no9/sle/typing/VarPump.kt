package za.co.no9.sle.typing

import za.co.no9.sle.Location


class VarPump {
    private var counter =
            0

    fun fresh(location: Location): TVar {
        val result =
                counter

        counter += 1

        return TVar(location, result)
    }
}