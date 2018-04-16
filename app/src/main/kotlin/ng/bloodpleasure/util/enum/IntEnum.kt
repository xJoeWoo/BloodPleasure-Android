package ng.bloodpleasure.util.enum

/**
 * Created by Ng on 16/04/2018.
 */
interface IntEnum : ValueEnum<Int>

inline fun <reified T> enumIntValueOfOrNull(value: Int?): T? where T : IntEnum, T : Enum<T> =
    if (value == null) null else enumValues<T>().find { it.value == value }