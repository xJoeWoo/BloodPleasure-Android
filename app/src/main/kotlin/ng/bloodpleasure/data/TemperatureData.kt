package ng.bloodpleasure.data

/**
 * Created by Ng on 16/04/2018.
 */
data class TemperatureData(
    val unit: TemperatureUnits,
    val status: TemperatureStatus,
    val mode: TemperatureModes,
    val value: Int
)

