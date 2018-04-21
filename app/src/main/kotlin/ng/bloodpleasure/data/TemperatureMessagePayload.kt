package ng.bloodpleasure.data

import ng.bloodpleasure.util.bt.BluetoothConnectionStatus

sealed class TemperatureMessagePayload {
    data class Data(val data: TemperatureData) : TemperatureMessagePayload()
    data class Error(val error: Throwable) : TemperatureMessagePayload()
    data class Connection(val connection: BluetoothConnectionStatus) : TemperatureMessagePayload()
}