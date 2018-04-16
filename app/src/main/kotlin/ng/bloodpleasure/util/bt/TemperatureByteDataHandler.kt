package ng.bloodpleasure.util.bt

import io.reactivex.Flowable
import ng.bloodpleasure.data.TemperatureData
import ng.bloodpleasure.data.TemperatureModes
import ng.bloodpleasure.data.TemperatureStatus
import ng.bloodpleasure.data.TemperatureUnits

/**
 * Created by Ng on 17/04/2018.
 */
interface TemperatureByteDataHandler {

    companion object {
        const val UNIT_FLAG = 2
        const val STATUS_FLAG = 3
        const val MODE_FLAG = 4
        const val TEMPERATURE_HIGH_FLAG = 5
        const val TEMPERATURE_LOW_FLAG = 6
    }

    fun Flowable<BytesCollector>.toTemperatureData(): Flowable<TemperatureData> =
        map {

            val values = it.buffer

            val unit =
                when (values[UNIT_FLAG]) {
                    0x1A.toByte() -> TemperatureUnits.Centigrade
                    0x15.toByte() -> TemperatureUnits.Fahrenheit
                    else -> throw IllegalArgumentException("Temp unit not supported")
                }

            val status =
                when (values[STATUS_FLAG]) {
                    0x81.toByte() -> TemperatureStatus.EXCEED
                    0x82.toByte() -> TemperatureStatus.BENEATH
                    0x83.toByte() -> TemperatureStatus.HARDWARE
                    0x84.toByte() -> TemperatureStatus.LOW_VOLTAGE
                    else -> TemperatureStatus.NORMAL
                }

            val mode =
                when (values[MODE_FLAG]) {
                    0x00.toByte() -> TemperatureModes.BODY
                    0x01.toByte() -> TemperatureModes.MEMORY
                    0x02.toByte() -> TemperatureModes.ROOM
                    0x03.toByte() -> TemperatureModes.SURFACE
                    else -> throw IllegalArgumentException("Temp mode not supported")
                }

            val value =
                values[TEMPERATURE_HIGH_FLAG] * 0x100 + values[TEMPERATURE_LOW_FLAG]

            TemperatureData(unit, status, mode, value)
        }


}