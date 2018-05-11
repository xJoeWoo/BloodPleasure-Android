package ng.bloodpleasure.util.bt

import java.util.*

/**
 * Created by Ng on 16/04/2018.
 */
interface BluetoothHandler {

    companion object {
        val SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB"
        val TD_133_READ_UUID = UUID.fromString("45531235-6565-7370-6f54-676e6f6c7553")
        val TD_133_WRITE_UUID = UUID.fromString("45531236-6565-7370-6f54-676e6f6c7553")
//        UUID(
//                BigInteger(TD_133_UUID_STR.substring(0, 16), 16).toLong(),
//                BigInteger(TD_133_UUID_STR.substring(16), 16).toLong()
//            )
    }

}


