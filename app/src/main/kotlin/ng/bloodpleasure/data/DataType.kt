package ng.bloodpleasure.data

import ng.bloodpleasure.util.enum.IntEnum

enum class DataTypes(override val value: Int) : IntEnum {
    TEMPERATURE(1),
    BLOOD_PRESSURE(2),
}

