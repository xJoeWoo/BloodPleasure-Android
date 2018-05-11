package ng.bloodpleasure.data

/**
 * Created by Ng on 16/04/2018.
 */
data class TemperatureData(
    val status: TemperatureStatus,
    val unit: TemperatureUnits = TemperatureUnits.CENTIGRADE,
    val mode: TemperatureModes = TemperatureModes.BODY,
    val value: Int = 0,
    val type: DataTypes = DataTypes.TEMPERATURE
)

