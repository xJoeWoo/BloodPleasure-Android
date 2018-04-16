package ng.bloodpleasure.data

sealed class TemperatureMessagePayload {
    data class Data(val data: TemperatureData) : TemperatureMessagePayload()
    data class Error(val throwable: Throwable) : TemperatureMessagePayload()
}