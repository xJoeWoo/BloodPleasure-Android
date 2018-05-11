package ng.bloodpleasure.data

import ng.bloodpleasure.util.enum.IntEnum

enum class TemperatureStatus(override val value: Int) : IntEnum {
    NORMAL(1),
    EXCEED(2),
    BENEATH(3),
    HARDWARE(4),
    LOW_VOLTAGE(5),
    OFF(6)
}