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
class TemperatureByteProcessor(val bytesCollector: BytesCollector) {

    companion object {
        private const val STATUS_FLAG = 2
        private const val DECIMAL_FLAG = 0x2e.toByte()
        private const val VALUE_FLAG = 0x3a.toByte()
        private val END_FLAGS = byteArrayOf(0, 0x0d, 0x0a)
        private const val TEMPERATURE_FLAG = 0x30.toByte()

        private const val MODE_OFFSET = -1
        private const val VALUE_OFFSET = 1
        private const val UNIT_OFFSET_FROM_TAIL = -3
        private const val STATUS_OFFSET_FROM_TAIL = -3

        private const val VALUE_MASK = 0x0F
    }

    private val bytes = bytesCollector.buffer
    private val length = bytes.size
    private val valueStartIndex = bytes.indexOf(VALUE_FLAG)

    fun process(): Observable<TemperatureData> {

        if (valueStartIndex == -1) {
            return TemperatureData(processStatus()).toObservable()
        }

        return TemperatureData(
            processStatus(),
            processUnit(),
            processMode(),
            processValue()
        ).toObservable()
    }

    private fun processValue(): Int {

        var value = 0
        var isDecimal = false

        for (index in valueStartIndex + VALUE_OFFSET until length) {
            val currentByte = bytes[index]

            if (currentByte == DECIMAL_FLAG) {
                isDecimal = true
                continue
            }

            if (currentByte.toInt() and VALUE_MASK.inv() != TEMPERATURE_FLAG.toInt() && isDecimal) {
                break
            }

            value *= 10
            value += currentByte.toInt() and VALUE_MASK
        }

        return value
    }


    private fun processMode(): TemperatureModes =
        when (bytes[valueStartIndex + MODE_OFFSET]) {
            0x79.toByte() -> TemperatureModes.BODY
            1.toByte() -> TemperatureModes.MEMORY
            0x6d.toByte() -> TemperatureModes.ROOM
            0x65.toByte() -> TemperatureModes.SURFACE
            else -> TemperatureModes.UNKNOWN
        }

    private fun processUnit(): TemperatureUnits =
        when (bytes[bytes.lastIndexOf(END_FLAGS[0]) - 1]) {
            0x43.toByte() -> TemperatureUnits.CENTIGRADE
            0x46.toByte() -> TemperatureUnits.FAHRENHEIT
            else -> TemperatureUnits.UNKNOWN
        }

    private fun processStatus(): TemperatureStatus =
        when {
            bytes.size >= 10 -> TemperatureStatus.NORMAL
            bytes.size == 1 && bytes.first() == 0.toByte() -> TemperatureStatus.OFF
            else ->
                when (bytes[bytes.lastIndexOf(END_FLAGS[0]) - 1]) {
                    0x48.toByte() -> TemperatureStatus.EXCEED
                    0x4c.toByte() -> TemperatureStatus.BENEATH
                    else -> TemperatureStatus.HARDWARE
                }
        }

}