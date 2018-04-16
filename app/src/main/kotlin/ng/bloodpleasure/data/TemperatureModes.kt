package ng.bloodpleasure.data

import ng.bloodpleasure.util.enum.IntEnum

enum class TemperatureModes(override val value: Int) : IntEnum {
    BODY(0),
    MEMORY(1),
    ROOM(2),
    SURFACE(3)
}