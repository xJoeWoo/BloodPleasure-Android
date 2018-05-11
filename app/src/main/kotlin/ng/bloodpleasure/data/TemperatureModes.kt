package ng.bloodpleasure.data

import ng.bloodpleasure.util.enum.IntEnum

enum class TemperatureModes(override val value: Int) : IntEnum {
    BODY(0x42),
    MEMORY(1),
    ROOM(0x52),
    SURFACE(0x66)
}