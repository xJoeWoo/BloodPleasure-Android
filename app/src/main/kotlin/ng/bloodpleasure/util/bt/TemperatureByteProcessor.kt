package ng.bloodpleasure.util.bt

import ng.bloodpleasure.data.TemperatureData
import ng.bloodpleasure.data.TemperatureModes
import ng.bloodpleasure.data.TemperatureStatus
import ng.bloodpleasure.data.TemperatureUnits

/**
 * Created by Ng on 17/04/2018.
 */
object TemperatureByteProcessor {

    const val UNIT_FLAG = 2
    const val STATUS_FLAG = 3
    const val MODE_FLAG = 4
    const val TEMPERATURE_HIGH_FLAG = 5
    const val TEMPERATURE_LOW_FLAG = 6

    fun process(bytesCollector: BytesCollector): TemperatureData {

        val values = bytesCollector.buffer

        val unit =
            when (values[UNIT_FLAG]) {
                TemperatureUnits.Centigrade.value.toByte() -> TemperatureUnits.Centigrade
                TemperatureUnits.Fahrenheit.value.toByte() -> TemperatureUnits.Fahrenheit
                else -> throw IllegalArgumentException("Temp unit not supported")
            }

        val status =
            when (values[STATUS_FLAG]) {
                TemperatureStatus.EXCEED.value.toByte() -> TemperatureStatus.EXCEED
                TemperatureStatus.BENEATH.value.toByte() -> TemperatureStatus.BENEATH
                TemperatureStatus.HARDWARE.value.toByte() -> TemperatureStatus.HARDWARE
                TemperatureStatus.LOW_VOLTAGE.value.toByte() -> TemperatureStatus.LOW_VOLTAGE
                else -> TemperatureStatus.NORMAL
            }

        val mode =
            when (values[MODE_FLAG]) {
                TemperatureModes.BODY.value.toByte() -> TemperatureModes.BODY
                TemperatureModes.MEMORY.value.toByte() -> TemperatureModes.MEMORY
                TemperatureModes.ROOM.value.toByte() -> TemperatureModes.ROOM
                TemperatureModes.SURFACE.value.toByte() -> TemperatureModes.SURFACE
                else -> throw IllegalArgumentException("Temp mode not supported")
            }

        val value =
            values[TEMPERATURE_HIGH_FLAG] * 0x100 + values[TEMPERATURE_LOW_FLAG]

        return TemperatureData(unit, status, mode, value)
    }

}