package ng.bloodpleasure.util.bt

enum class BluetoothConnectionStatus {
    PAIRED,
    DISCOVERING,
    NOT_FOUND,
    FOUND,
    CONNECTING,
    CONNECTED,
    CONNECT_FAILED,
    READ_ERROR,
    WRITE_ERROR
}