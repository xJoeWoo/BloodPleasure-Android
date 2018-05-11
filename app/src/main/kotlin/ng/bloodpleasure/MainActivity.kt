package ng.bloodpleasure

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import com.polidea.rxandroidble2.RxBleClient
import io.reactivex.disposables.Disposable
import io.reactivex.plugins.RxJavaPlugins
import ng.bloodpleasure.data.Temperature
import ng.bloodpleasure.data.TemperatureMessage
import ng.bloodpleasure.data.TemperatureMessagePayload
import ng.bloodpleasure.data.TemperatureMessageStatus
import ng.bloodpleasure.ui.MainActivityUi
import ng.bloodpleasure.ui.webview.BpJsInterface
import ng.bloodpleasure.ui.webview.BpWebChromeClient
import ng.bloodpleasure.ui.webview.BpWebViewClient
import ng.bloodpleasure.util.*
import ng.bloodpleasure.util.activity.BaseActivity
import ng.bloodpleasure.util.bt.TemperatureBluetoothHandler
import org.jetbrains.anko.longToast
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.toast


@SuppressLint("HardwareIds")
class MainActivity : BaseActivity() {

    private val rxBluetooth: RxBleClient by lazy { RxBleClient.create(this) }
    private lateinit var ui: MainActivityUi
    private var temperatureBluetoothHandler: TemperatureBluetoothHandler? = null
    private var hardwareSubscription: Disposable? = null
    private var hardwareConnectionSubscription: Disposable? = null

    private val androidId: String
            by lazy {
                Settings.Secure.getString(
                    contentResolver,
                    Settings.Secure.ANDROID_ID
                )
            }

    companion object {
        const val REQUEST_ENABLE_BT = 1
        const val REQUEST_PERMISSION_COARSE_LOCATION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RxJavaPlugins.setErrorHandler { it.e("ErrorHandler") }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        ui = MainActivityUi(
            BpJsInterface(androidId, { onPageReady() }, { onPageMute() }),
            BpWebViewClient(),
            BpWebChromeClient()
        )
        ui.setContentView(this)
        ui.webView.loadUrl("http://t.zoyoo.me/")
    }

    private fun onPageReady() {
        subscribeHardware()
        connect()
    }

    private fun onPageMute() {
        unsubscribeHardware()
    }

    private fun subscribeHardware() {
        unsubscribeHardware()
        hardwareSubscription = Temperature.flowable.subscribe {
            it.e("Result")
            ui.webView.evaluateJavascript(
                jsMethod(
                    BpJsInterface.JS_HARDWARE_VALUE_METHOD_NAME,
                    it.toJson().wrapWithSingleQuotation(),
                    androidId.wrapWithSingleQuotation()
                )
            ) {

            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION_COARSE_LOCATION -> connect()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_ENABLE_BT -> {
                if (resultCode == Activity.RESULT_OK) {
                    connect()
                } else {
                    toast("请开启蓝牙以检测")
                }
            }
        }
    }

    fun connect() {
        disconnect()
//        if (isDebugging) {
//            hardwareConnectionSubscription = Temperature.publish(
//                Flowable
//                    .interval(0, 3000, TimeUnit.MILLISECONDS)
//                    .map {
//                        TemperatureMessage(
//                            TemperatureMessageStatus.NORMAL, TemperatureMessagePayload.Data(
//                                TemperatureData(
//                                    TemperatureUnits.Centigrade,
//                                    TemperatureStatus.NORMAL,
//                                    TemperatureModes.BODY,
//                                    (350..420).random()
//                                )
//                            )
//                        )
//                    }
//            )
//            return
//        }


        when (rxBluetooth.state!!) {

            RxBleClient.State.BLUETOOTH_NOT_AVAILABLE -> longToast("设备上没有蓝牙模块")

            RxBleClient.State.LOCATION_PERMISSION_NOT_GRANTED, RxBleClient.State.LOCATION_SERVICES_NOT_ENABLED ->
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                    REQUEST_PERMISSION_COARSE_LOCATION
                )

            RxBleClient.State.BLUETOOTH_NOT_ENABLED ->
                startActivityForResult(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    REQUEST_ENABLE_BT
                )

            RxBleClient.State.READY -> TemperatureBluetoothHandler(rxBluetooth)
                .apply {
                    hardwareConnectionSubscription =
                            connectionStatus
                                .doOnNext {
                                    it.e("BluetoothConnectionStatus")
                                }
                                .map {
                                    TemperatureMessage(
                                        TemperatureMessageStatus.CONNECTION,
                                        TemperatureMessagePayload.Connection(it)
                                    )
                                }
                                .subscribe { Temperature.publish(it.toObservable()) }
                }
                .connect()
        }

    }

    private fun disconnect() {
        hardwareConnectionSubscription.safeDispose()
        temperatureBluetoothHandler?.dispose()
    }

    private fun unsubscribeHardware() {
        hardwareSubscription.safeDispose()
    }

    override fun onResume() {
        super.onResume()
        ui.jsStatus(true)
    }

    override fun onStop() {
        super.onStop()
        ui.jsStatus(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
        unsubscribeHardware()
//        rxBluetooth.dispose()
    }

}
