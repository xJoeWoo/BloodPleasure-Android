package ng.bloodpleasure.util.bt

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.github.ivbaranov.rxbluetooth.BluetoothConnection
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import ng.bloodpleasure.data.Temperature
import ng.bloodpleasure.util.*
import ng.bloodpleasure.util.bt.BluetoothHandler.Companion.SPP_UUID

/**
 * Created by Ng on 16/04/2018.
 */
class TemperatureBluetoothHandler(
    private val rxBluetooth: RxBluetooth
) : BluetoothHandler {

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

    private var bluetoothConnection: BluetoothConnection? = null

    private val subject = PublishSubject.create<BluetoothConnectionStatus>()

    val observeConnectionStatus: Observable<BluetoothConnectionStatus> = subject

    fun connect() {

        dispose()

        bluetoothStateSubscribe = rxBluetooth.observeBluetoothState()
            .observeOnComputation()
            .subscribe {
                it.e("BluetoothState")
            }

        discoveryStateSubscribe = rxBluetooth.observeDiscovery()
            .observeOnComputation()
            .subscribe {
                it.e("DiscoveryState")
            }

        val targetDevice = rxBluetooth.bondedDevices.firstOrNull { it.address == DEVICE_ADDRESS }

        if (targetDevice != null) {
            subject.onNext(BluetoothConnectionStatus.PAIRED)
            temperatureSubscribe = targetDevice.toObservable().createConnection()
            return
        }

        if (rxBluetooth.isDiscovering) {
            rxBluetooth.cancelDiscovery()
        }

        readSubscribe =
                rxBluetooth.observeDevices()
                    .observeOnComputation()
                    .filter { it.address == DEVICE_ADDRESS }
                    .firstOrError()
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
                    }
                    .createConnection()

        subject.onNext(BluetoothConnectionStatus.DISCOVERING)
        rxBluetooth.startDiscovery()
    }


    private fun Observable<BluetoothDevice>.createConnection(): Disposable =
        apply { subject.onNext(BluetoothConnectionStatus.CONNECTING) }
            .flatMap { rxBluetooth.observeConnectDevice(it, SPP_UUID) }
            .observeOnIO()
            .doOnNext { subject.onNext(BluetoothConnectionStatus.CONNECTED) }
            .doOnError {
                it.e("ConnectDevice")
                subject.onNext(BluetoothConnectionStatus.CONNECT_FAILED)
                dispose()
            }
            .readDevice()


    private fun Observable<BluetoothSocket>.readDevice(): Disposable =
        subscribe {

            bluetoothConnection = BluetoothConnection(it).apply {

                temperatureSubscribe = observeByteStream()
                    .onErrorResumeNext(Flowable.empty())
                    .observeOnIO()
                    .scan(BytesCollector(LENGTH)) { collector, value ->
                        collector.apply {
                            if (isCompleted) {
                                currentLength = 0
                            }
                            var passed = false
                            when (currentLength) {
                                0 -> if (value == HEAD_FIRST_BYTE) passed = true
                                1 -> if (value == HEAD_SECOND_BYTE) passed = true
                                LENGTH - TAIL_LENGTH - 0 -> if (value == TAIL_FIRST_BYTE) passed =
                                        true
                                LENGTH - TAIL_LENGTH + 1 -> if (value == TAIL_SECOND_BYTE) passed =
                                        true
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
                    .flatMap { TemperatureByteProcessor.process(it) }
                    .doOnNext {
                        it.e("Data")
                    }
                    .doOnError {
                        it.e("BluetoothRead")
                        subject.onNext(BluetoothConnectionStatus.READ_ERROR)
                        dispose()
                    }
                    .let {
                        Temperature.publish(it)
                    }
            }
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