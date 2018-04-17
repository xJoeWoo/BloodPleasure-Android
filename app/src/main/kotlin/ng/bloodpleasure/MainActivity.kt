package ng.bloodpleasure

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import io.reactivex.disposables.Disposable
import ng.bloodpleasure.data.Temperature
import ng.bloodpleasure.ui.MainActivityUi
import ng.bloodpleasure.ui.webview.BpJsInterface
import ng.bloodpleasure.ui.webview.BpWebChromeClient
import ng.bloodpleasure.ui.webview.BpWebViewClient
import ng.bloodpleasure.util.activity.BaseActivity
import ng.bloodpleasure.util.bt.EnableStatus
import ng.bloodpleasure.util.bt.TemperatureBluetoothHandler
import ng.bloodpleasure.util.bt.dispose
import ng.bloodpleasure.util.bt.enableStatus
import ng.bloodpleasure.util.e
import ng.bloodpleasure.util.jsMethod
import ng.bloodpleasure.util.safeDispose
import ng.bloodpleasure.util.toJson
import org.jetbrains.anko.longToast
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.toast

class MainActivity : BaseActivity() {

    private val rxBluetooth: RxBluetooth by lazy { RxBluetooth(this) }
    private lateinit var ui: MainActivityUi
    private var temperatureBluetoothHandler: TemperatureBluetoothHandler? = null
    private var hardwareSubscription: Disposable? = null
    private var hardwareConnectionSubscription: Disposable? = null

    companion object {
        const val REQUEST_ENABLE_BT = 1
        const val REQUEST_PERMISSION_COARSE_LOCATION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = MainActivityUi(BpJsInterface(), BpWebViewClient(), BpWebChromeClient())
        ui.setContentView(this)

        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_PERMISSION_COARSE_LOCATION
            )
        } else {
            connect()
        }

        subscribeHardware()
    }

    private fun subscribeHardware() {
        hardwareSubscription = Temperature.flowable.subscribe {
            it.e("Result")
            ui.webView.evaluateJavascript(
                jsMethod(BpJsInterface.JS_HARDWARE_VALUE_METHOD_NAME, it.toJson())
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
                    toast("请开启蓝牙使用")
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ui.jsStatus(true)
    }


    fun connect() {
        when (rxBluetooth.enableStatus) {
            EnableStatus.NO_MODULE -> {
                longToast("设备上没有蓝牙模块")
                finish()
            }

            EnableStatus.NOT_ENABLED -> {
                rxBluetooth.enableBluetooth(this, REQUEST_ENABLE_BT)
            }

            EnableStatus.ENABLED -> {
                temperatureBluetoothHandler = TemperatureBluetoothHandler(rxBluetooth).apply {
                    hardwareConnectionSubscription =
                            observeConnectionStatus
                                .subscribe { it.e("BluetoothConnectionStatus") }
                                .also {
                                    connect()
                                }
                }
            }
        }
    }


    override fun onStop() {
        super.onStop()
        ui.jsStatus(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        hardwareSubscription.safeDispose()
        hardwareConnectionSubscription.safeDispose()
        temperatureBluetoothHandler?.dispose()
        rxBluetooth.dispose()
    }

}
