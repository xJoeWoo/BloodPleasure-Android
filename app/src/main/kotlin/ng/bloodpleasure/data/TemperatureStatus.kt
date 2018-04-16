package ng.bloodpleasure.data

import ng.bloodpleasure.util.enum.IntEnum

enum class TemperatureStatus(override val value: Int) : IntEnum {
    NORMAL(0),
    EXCEED(0x81),
    BENEATH(0x82),
    HARDWARE(0x83),
    LOW_VOLTAGE(0x84)
}