package ng.bloodpleasure.data

data class TemperatureMessage(
    val status: TemperatureMessageStatus,
    val payload: TemperatureMessagePayload
)