package ng.bloodpleasure.util.bt

import io.reactivex.Observable
import ng.bloodpleasure.data.TemperatureData
import ng.bloodpleasure.data.TemperatureModes
import ng.bloodpleasure.data.TemperatureStatus
import ng.bloodpleasure.data.TemperatureUnits
import ng.bloodpleasure.util.toObservable

/**
 * Created by Ng on 17/04/2018.
 */
object TemperatureByteProcessor {

    const val UNIT_FLAG = 9
    const val STATUS_FLAG = 0
    const val MODE_FLAG = 0
    const val TEMPERATURE_TENS_FLAG = 5
    const val TEMPERATURE_ONES_FLAG = 6
    const val TEMPERATURE_DECIMAL_ONES_FLAG = 8
    const val TEMPERATURE_MASK = 0x0F

    fun process(bytesCollector: BytesCollector): Observable<TemperatureData> {

        var isException = false

        val length = bytesCollector.buffer.size

        val values =

            when {
                length > 13 -> bytesCollector.buffer.sliceArray(3 until length)
                length == 13 -> bytesCollector.buffer
                length == 1 && bytesCollector.buffer.first() == 0.toByte() ->
                    return TemperatureData(
                        TemperatureUnits.Centigrade,
                        TemperatureStatus.OFF,
                        TemperatureModes.BODY,
                        0
                    ).toObservable()

                else -> {
                    isException = true
                    bytesCollector.buffer
                }
            }


        if (isException) {
            val status =
                when (values[STATUS_FLAG]) {
                    TemperatureStatus.EXCEED.value.toByte() -> TemperatureStatus.EXCEED
                    else -> TemperatureStatus.BENEATH
                }

            return TemperatureData(
                TemperatureUnits.Centigrade,
                status,
                TemperatureModes.BODY,
                0
            ).toObservable()
        }

        val unit =
            when (values[UNIT_FLAG]) {
                TemperatureUnits.Centigrade.value.toByte() -> TemperatureUnits.Centigrade
                TemperatureUnits.Fahrenheit.value.toByte() -> TemperatureUnits.Fahrenheit
                else -> return Observable.empty()
            }


        val mode =
            when (values[MODE_FLAG]) {
                TemperatureModes.BODY.value.toByte() -> TemperatureModes.BODY
                TemperatureModes.MEMORY.value.toByte() -> TemperatureModes.MEMORY
                TemperatureModes.ROOM.value.toByte() -> TemperatureModes.ROOM
                TemperatureModes.SURFACE.value.toByte() -> TemperatureModes.SURFACE
                else -> return Observable.empty()
            }

        val tensValue =
            (values[TEMPERATURE_TENS_FLAG].toInt() and TEMPERATURE_MASK).let {
                if (it <= 1)
                    it * 1000
                else
                    it * 100
            }

        val value =
            tensValue +
                    (values[TEMPERATURE_ONES_FLAG].toInt() and TEMPERATURE_MASK) * 10 +
                    (values[TEMPERATURE_DECIMAL_ONES_FLAG].toInt() and TEMPERATURE_MASK)

        return TemperatureData(unit, TemperatureStatus.NORMAL, mode, value).toObservable()


    }

}