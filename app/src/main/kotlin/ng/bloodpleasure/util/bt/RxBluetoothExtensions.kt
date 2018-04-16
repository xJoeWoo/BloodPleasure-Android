package ng.bloodpleasure.util.bt

import com.github.ivbaranov.rxbluetooth.RxBluetooth

/**
 * Created by Ng on 16/04/2018.
 */

val RxBluetooth.enableStatus
    get() = when {
        !isBluetoothAvailable -> EnableStatus.NO_MODULE
        !isBluetoothEnabled -> EnableStatus.NOT_ENABLED
        else -> EnableStatus.ENABLED
    }

fun RxBluetooth.dispose() {
    cancelDiscovery()
}


enum class EnableStatus {
    NO_MODULE,
    ENABLED,
    NOT_ENABLED
}