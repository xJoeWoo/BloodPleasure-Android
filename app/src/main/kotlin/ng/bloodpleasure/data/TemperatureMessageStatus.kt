package ng.bloodpleasure.data

import ng.bloodpleasure.util.enum.IntEnum

enum class TemperatureMessageStatus(override val value: Int) : IntEnum {
    NORMAL(1),
    ERROR(2),
    CONNECTION(3)
}