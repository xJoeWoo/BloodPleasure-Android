package ng.bloodpleasure.ui.webview

import android.webkit.JavascriptInterface

@Suppress("unused")
/**
 * Created by Ng on 16/04/2018.
 */
class BpJsInterface(private val hardwareId: String, private val onPageReady: () -> Unit) {

    companion object {
        const val JS_CLASS_NAME: String = "BloodPleasure"
        const val JS_HARDWARE_VALUE_METHOD_NAME = "hardwareValue"
    }

    @JavascriptInterface
    fun hardwareId(): String = hardwareId

    @JavascriptInterface
    fun pageReady() {
        onPageReady()
    }

}

