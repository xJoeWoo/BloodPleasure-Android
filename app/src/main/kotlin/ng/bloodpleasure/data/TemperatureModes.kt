package ng.bloodpleasure.data

import ng.bloodpleasure.util.enum.IntEnum

enum class TemperatureModes(override val value: Int) : IntEnum {
    BODY(1),
    MEMORY(2),
    ROOM(3),
    SURFACE(4),
    UNKNOWN(0)
}