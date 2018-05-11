package ng.bloodpleasure.data

import ng.bloodpleasure.util.enum.IntEnum

enum class TemperatureUnits(override val value: Int) : IntEnum {
    CENTIGRADE(1),
    FAHRENHEIT(2),
    UNKNOWN(0)
}