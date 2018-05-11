package ng.bloodpleasure.util.bt

import com.github.ivbaranov.rxbluetooth.BluetoothConnection
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.toObservable
import io.reactivex.subjects.PublishSubject
import ng.bloodpleasure.data.Temperature
import ng.bloodpleasure.data.TemperatureMessage
import ng.bloodpleasure.data.TemperatureMessagePayload
import ng.bloodpleasure.data.TemperatureMessageStatus
import ng.bloodpleasure.util.*
import ng.bloodpleasure.util.bt.BluetoothHandler.Companion.TD_133_WRITE_UUID

/**
 * Created by Ng on 16/04/2018.
 */
class TemperatureBluetoothHandler(
    private val rxBluetooth: RxBleClient
) : BluetoothHandler {

    companion object {
        const val DEVICE_ADDRESS = "98:D3:31:FB:5E:88"
        const val DEVICE_NAME = "E-Smoke"
        const val LENGTH = 9

        const val HEAD_FIRST_BYTE = 0xFE.toByte()
        const val HEAD_SECOND_BYTE = 0xFD.toByte()
        const val TAIL_FIRST_BYTE = 0x0D.toByte()
        const val TAIL_SECOND_BYTE = 0x0A.toByte()
        const val HEAD_LENGTH = 2
        const val TAIL_LENGTH = 2
        val PAYLOAD_RANGE = HEAD_LENGTH until LENGTH - TAIL_LENGTH
    }

    private var discoveryStateSubscribe: Disposable? = null
    private var discoveryDevicesSubscribe: Disposable? = null
    private var bluetoothStateSubscribe: Disposable? = null
    private var connectDeviceSubscribe: Disposable? = null
    private var readSubscribe: Disposable? = null
    private var writeSubscribe: Disposable? = null
    private var temperatureSubscribe: Disposable? = null

    private var bluetoothConnection: BluetoothConnection? = null

    private val subject = PublishSubject.create<BluetoothConnectionStatus>()

    val connectionStatus: Observable<BluetoothConnectionStatus> = subject

    fun connect() {

        dispose()

        bluetoothStateSubscribe = rxBluetooth.observeStateChanges()
            .observeOnComputation()
            .subscribe {
                it.e("BluetoothState")
            }
//
//        discoveryStateSubscribe = rxBluetooth.observeDiscovery()
//            .observeOnComputation()
//            .subscribe {
//                it.e("DiscoveryState")
//            }

//        val targetDevice = rxBluetooth.bondedDevices.firstOrNull { it.address == DEVICE_ADDRESS }
        val targetDevice = rxBluetooth.bondedDevices.firstOrNull { it.name == DEVICE_NAME }

        if (targetDevice != null) {
            subject.onNext(BluetoothConnectionStatus.PAIRED)
            temperatureSubscribe = targetDevice.toObservable().createConnection()
            return
        }
//
//        if (rxBluetooth.isDiscovering) {
//            rxBluetooth.cancelDiscovery()
//        }

        subject.onNext(BluetoothConnectionStatus.DISCOVERING)

        temperatureSubscribe =
                rxBluetooth.scanBleDevices(ScanSettings.Builder().build())
                    .observeOnComputation()
//                    .filter { it.address == DEVICE_ADDRESS }
                    .filter { it.bleDevice.name == DEVICE_NAME }
                    .firstOrError()
                    .map { it.bleDevice }
                    .toObservable()
                    .doOnNext {
                        if (it != null) {
                            subject.onNext(BluetoothConnectionStatus.FOUND)

                        } else {
                            subject.onNext(BluetoothConnectionStatus.NOT_FOUND)
                        }
                    }
                    .doOnError {
                        it.e("DiscoveryDevice")
                        subject.onNext(BluetoothConnectionStatus.NOT_FOUND)
                        dispose()
                    }
                    .createConnection()


//        rxBluetooth.startDiscovery()
    }


    private fun Observable<RxBleDevice>.createConnection(): Disposable =
        doOnNext {
            connectDeviceSubscribe = it.observeConnectionStateChanges()
                .subscribe {
                    it.e("BleConnectionState")
                    subject.onNext(
                        when (it!!) {
                            RxBleConnection.RxBleConnectionState.CONNECTING -> BluetoothConnectionStatus.CONNECTING
                            RxBleConnection.RxBleConnectionState.CONNECTED -> BluetoothConnectionStatus.CONNECTED
                            RxBleConnection.RxBleConnectionState.DISCONNECTED -> BluetoothConnectionStatus.DISCONNECTED
                            RxBleConnection.RxBleConnectionState.DISCONNECTING -> BluetoothConnectionStatus.DISCONNECTING
                        }
                    )
                }
        }
            .switchMap {
                it.establishConnection(false)
//                rxBluetooth.observeConnectDevice(
//                    it,
//                    TD_133_UUID.first().also { it.toString().e("asdf") })
            }
//            .map { it.createInsecureRfcommSocketToServiceRecord(SPP_UUID) }
            .observeOnIO()
            .doOnError {
                it.e("ConnectDevice")
                subject.onNext(BluetoothConnectionStatus.CONNECT_FAILED)
                dispose()
            }
//            .subscribe { conn ->
//
//                conn.discoverServices()
//                    .toObservable()
//                    .flatMap { it.bluetoothGattServices.toObservable() }
//                    .flatMap { it.characteristics.toObservable() }
//                    .filter { it.uuid == TD_133_WRITE_UUID }
//                    .subscribe({
//                        conn.setupNotification(it)
//                            .subscribe {
//
//                                it.subscribe({
//                                    it.toHex().e("SETUP")
//                                },
//                                    { it.e(); })
//
//                                conn.readCharacteristic(uuid)
//                                    .subscribe({ (it.toHex() + "$uuid").w() },
//                                        { it.e(); })
//                            }
//                    },
//                        { it.e(); })
//
//
//            }
            .readDevice()


    private fun Observable<RxBleConnection>.readDevice(): Disposable =

        flatMap { conn ->
            conn.discoverServices().toObservable().map { conn to it }
        }
            .flatMap { (conn, it) ->
                it.bluetoothGattServices
                    .flatMap { it.characteristics }
                    .map { conn to it }
                    .toObservable()
            }
            .filter { (_, it) -> it.uuid == TD_133_WRITE_UUID }
            .flatMap { (conn, it) ->
                conn.setupNotification(it).flatMap { it }
            }
            .observeOnIO()
//            .scan(BytesCollector(LENGTH)) { collector, value ->
//                collector.apply {
//                    if (isCompleted) {
//                        currentLength = 0
//                    }
//                    var passed = false
//                    when (currentLength) {
//                        0 -> if (value == HEAD_FIRST_BYTE) passed = true
//                        1 -> if (value == HEAD_SECOND_BYTE) passed = true
//                        LENGTH - TAIL_LENGTH - 0 -> if (value == TAIL_FIRST_BYTE) passed =
//                                true
//                        LENGTH - TAIL_LENGTH + 1 -> if (value == TAIL_SECOND_BYTE) passed =
//                                true
//                        in PAYLOAD_RANGE -> passed = true
//                    }
//
//                    if (!passed) {
//                        currentLength = 0
//                    } else {
//                        buffer[currentLength++] = value
//                    }
//                }
//            }
//            .filter { it.isCompleted }
            .map { it.e("RawData"); BytesCollector(LENGTH, it.size, it) }
            .flatMap { TemperatureByteProcessor.process(it) }
            .doOnNext {
                it.e("Data")
            }
            .doOnError {
                it.e("BluetoothRead")
                subject.onNext(BluetoothConnectionStatus.READ_ERROR)
                dispose()
            }
            .map {
                TemperatureMessage(
                    TemperatureMessageStatus.NORMAL,
                    TemperatureMessagePayload.Data(it)
                )
            }
            .let {
                Temperature.publish(it)
            }


    fun dispose() {
        temperatureSubscribe.safeDispose()
        writeSubscribe.safeDispose()
        readSubscribe.safeDispose()
        connectDeviceSubscribe.safeDispose()
        bluetoothStateSubscribe.safeDispose()
        discoveryDevicesSubscribe.safeDispose()
        discoveryStateSubscribe.safeDispose()
        bluetoothConnection?.closeConnection()
    }

}