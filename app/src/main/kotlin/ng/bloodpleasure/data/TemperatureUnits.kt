package ng.bloodpleasure.data

import ng.bloodpleasure.util.enum.IntEnum

enum class TemperatureUnits(override val value: Int) : IntEnum {
    Centigrade(0x43),
    Fahrenheit(0x46)
}