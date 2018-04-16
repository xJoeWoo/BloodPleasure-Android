package ng.bloodpleasure.util.bt

import android.bluetooth.BluetoothDevice
import com.github.ivbaranov.rxbluetooth.BluetoothConnection
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import ng.bloodpleasure.data.Temperature
import ng.bloodpleasure.util.bt.BluetoothHandler.Companion.SPP_UUID
import ng.bloodpleasure.util.e
import ng.bloodpleasure.util.observeOnComputationSubscribeOnMain
import ng.bloodpleasure.util.observeOnIOSubscribeOnMain
import ng.bloodpleasure.util.safeDispose

/**
 * Created by Ng on 16/04/2018.
 */
class TemperatureBluetoothHandler(
    private val rxBluetooth: RxBluetooth
) : BluetoothHandler, TemperatureByteDataHandler {

    companion object {
        const val DEVICE_ADDRESS = "98:D3:31:FB:5E:88"
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

    private val subject = PublishSubject.create<BluetoothConnectionStatus>()

    val observeConnectionStatus: Observable<BluetoothConnectionStatus> = subject

    fun connect() {

        dispose()

        bluetoothStateSubscribe = rxBluetooth.observeBluetoothState()
            .observeOnComputationSubscribeOnMain()
            .subscribe {
                it.e("BluetoothState")
            }

        discoveryStateSubscribe = rxBluetooth.observeDiscovery()
            .observeOnComputationSubscribeOnMain()
            .subscribe {
                it.e("DiscoveryState")
            }

        val targetDevice = rxBluetooth.bondedDevices.firstOrNull { it.address == DEVICE_ADDRESS }

        if (targetDevice != null) {
            subject.onNext(BluetoothConnectionStatus.PAIRED)
            createConnection(targetDevice)
            return
        }

        if (rxBluetooth.isDiscovering) {
            rxBluetooth.cancelDiscovery()
        }

        discoveryDevicesSubscribe =
                rxBluetooth.observeDevices()
                    .filter { it.address == DEVICE_ADDRESS }
                    .observeOnComputationSubscribeOnMain()
                    .firstOrError()
                    .subscribe({ device: BluetoothDevice? ->
                        if (device != null) {
                            subject.onNext(BluetoothConnectionStatus.FOUND)
                            createConnection(device)
                        } else {
                            subject.onNext(BluetoothConnectionStatus.NOT_FOUND)
                        }
                        discoveryDevicesSubscribe.safeDispose()
                    }, {
                        it.e("DiscoveryDevice")
                        subject.onNext(BluetoothConnectionStatus.NOT_FOUND)
                    })


        subject.onNext(BluetoothConnectionStatus.DISCOVERING)
        rxBluetooth.startDiscovery()

    }


    private fun createConnection(device: BluetoothDevice) {
        subject.onNext(BluetoothConnectionStatus.CONNECTING)
        connectDeviceSubscribe = rxBluetooth.observeConnectDevice(device, SPP_UUID)
            .observeOnIOSubscribeOnMain()
            .subscribe({
                subject.onNext(BluetoothConnectionStatus.CONNECTED)
                readDevice(BluetoothConnection(it))
            }, {
                it.e("ConnectDevice")
                subject.onNext(BluetoothConnectionStatus.CONNECT_FAILED)
                dispose()
            })

    }

    private fun readDevice(connection: BluetoothConnection) {

        val dataFlowable = connection.observeByteStream()
            .observeOnIOSubscribeOnMain()
            .scan(BytesCollector(LENGTH)) { collector, value ->
                collector.apply {
                    if (isCompleted) {
                        currentLength = 0
                    }
                    var passed = false
                    when (currentLength) {
                        0 -> if (value == HEAD_FIRST_BYTE) passed = true
                        1 -> if (value == HEAD_SECOND_BYTE) passed = true
                        LENGTH - TAIL_LENGTH - 0 -> if (value == TAIL_FIRST_BYTE) passed = true
                        LENGTH - TAIL_LENGTH + 1 -> if (value == TAIL_SECOND_BYTE) passed = true
                        in PAYLOAD_RANGE -> passed = true
                    }

                    if (!passed) {
                        currentLength = 0
                    } else {
                        buffer[currentLength++] = value
                    }
                }
            }
            .filter { it.isCompleted }
            .toTemperatureData()
            .publish()
            .autoConnect(2)

        temperatureSubscribe = Temperature.connect(dataFlowable)

        readSubscribe = dataFlowable
            .subscribe({
                it.e("Data")
            }, {
                it.e("BluetoothRead")
                subject.onNext(BluetoothConnectionStatus.READ_ERROR)
                dispose()
            })

    }

    fun dispose() {
        discoveryDevicesSubscribe.safeDispose()
        discoveryStateSubscribe.safeDispose()
        bluetoothStateSubscribe.safeDispose()
        connectDeviceSubscribe.safeDispose()
        readSubscribe.safeDispose()
        writeSubscribe.safeDispose()
        temperatureSubscribe.safeDispose()
    }

}