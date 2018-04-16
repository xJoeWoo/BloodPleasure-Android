package ng.bloodpleasure.data

import ng.bloodpleasure.util.enum.IntEnum

/**
 * Created by Ng on 16/04/2018.
 */
data class TemperatureData(
    val unit: TemperatureUnits,
    val status: TemperatureStatus,
    val mode: TemperatureModes,
    val value: Int
)

enum class TemperatureUnits(override val value: Int) : IntEnum {
    Centigrade(1),
    Fahrenheit(2)
}

enum class TemperatureStatus(override val value: Int) : IntEnum {
    NORMAL(0),
    EXCEED(0x81),
    BENEATH(0x82),
    HARDWARE(0x83),
    LOW_VOLTAGE(0x84)
}

enum class TemperatureModes(override val value: Int) : IntEnum {
    BODY(0),
    MEMORY(1),
    ROOM(2),
    SURFACE(3)
}