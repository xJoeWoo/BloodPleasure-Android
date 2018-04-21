package ng.bloodpleasure

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import ng.bloodpleasure.data.*
import ng.bloodpleasure.ui.MainActivityUi
import ng.bloodpleasure.ui.webview.BpJsInterface
import ng.bloodpleasure.ui.webview.BpWebChromeClient
import ng.bloodpleasure.ui.webview.BpWebViewClient
import ng.bloodpleasure.util.*
import ng.bloodpleasure.util.activity.BaseActivity
import ng.bloodpleasure.util.bt.EnableStatus
import ng.bloodpleasure.util.bt.TemperatureBluetoothHandler
import ng.bloodpleasure.util.bt.dispose
import ng.bloodpleasure.util.bt.enableStatus
import org.jetbrains.anko.longToast
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.toast
import java.util.concurrent.TimeUnit

@SuppressLint("HardwareIds")
class MainActivity : BaseActivity() {

    private val rxBluetooth: RxBluetooth by lazy { RxBluetooth(this) }
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

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        ui = MainActivityUi(
            BpJsInterface(androidId, { onPageReady() }),
            BpWebViewClient(),
            BpWebChromeClient()
        )
        ui.setContentView(this)
        ui.webView.loadUrl("http://t.zoyoo.me/")
        subscribeHardware()
    }

    private fun onPageReady() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_PERMISSION_COARSE_LOCATION
            )
        } else {
            connect()
        }
    }

    private fun subscribeHardware() {
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

    override fun onResume() {
        super.onResume()
        ui.jsStatus(true)
    }


    fun connect() {
        disconnect()
        if (isDebugging) {
            hardwareConnectionSubscription = Temperature.publish(
                Flowable
                    .interval(0, 3000, TimeUnit.MILLISECONDS)
                    .map {
                        TemperatureMessage(
                            TemperatureMessageStatus.NORMAL, TemperatureMessagePayload.Data(
                                TemperatureData(
                                    TemperatureUnits.Centigrade,
                                    TemperatureStatus.NORMAL,
                                    TemperatureModes.BODY,
                                    (0..500).random()
                                )
                            )
                        )
                    }
            )
            return
        }

        when (rxBluetooth.enableStatus) {
            EnableStatus.NO_MODULE -> {
                longToast("设备上没有蓝牙模块")
            }

            EnableStatus.NOT_ENABLED -> {
                rxBluetooth.enableBluetooth(this, REQUEST_ENABLE_BT)
            }

            EnableStatus.ENABLED -> {
                temperatureBluetoothHandler = TemperatureBluetoothHandler(rxBluetooth).apply {
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
                                .let { Temperature.publish(it.toFlowable(BackpressureStrategy.LATEST)) }
                                .also {
                                    connect()
                                }
                }
            }
        }
    }

    private fun disconnect() {
        hardwareConnectionSubscription.safeDispose()
        temperatureBluetoothHandler?.dispose()
    }

    override fun onStop() {
        super.onStop()
        ui.jsStatus(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
        hardwareSubscription.safeDispose()
        rxBluetooth.dispose()
    }

}
