package ng.bloodpleasure.util.bt

import ng.bloodpleasure.util.enum.IntEnum

enum class BluetoothConnectionStatus(override val value: Int) : IntEnum {
    PAIRED(1),
    DISCOVERING(2),
    NOT_FOUND(3),
    FOUND(4),
    CONNECTING(5),
    CONNECTED(6),
    CONNECT_FAILED(7),
    DISCONNECTED(8),
    DISCONNECTING(9),
    READ_ERROR(10),
    WRITE_ERROR(11),
    UNKNOWN(0)
}