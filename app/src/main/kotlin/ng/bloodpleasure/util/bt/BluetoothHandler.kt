package ng.bloodpleasure.util.bt

import java.math.BigInteger
import java.util.*

/**
 * Created by Ng on 16/04/2018.
 */
interface BluetoothHandler {

    companion object {
        val SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB"
        private const val TD_133_UUID_STR = "45531234656573706f54676e6f6c7553"
        val TD_133_UUID = listOf(
            UUID(
                BigInteger(TD_133_UUID_STR.substring(0, 16), 16).toLong(),
                BigInteger(TD_133_UUID_STR.substring(16), 16).toLong()
            )
        )
    }

}


