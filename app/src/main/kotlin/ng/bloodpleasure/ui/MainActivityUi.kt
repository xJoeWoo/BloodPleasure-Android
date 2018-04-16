package ng.bloodpleasure.ui

import android.annotation.SuppressLint
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import ng.bloodpleasure.MainActivity
import ng.bloodpleasure.ui.webview.BpJsInterface
import ng.bloodpleasure.ui.webview.BpWebChromeClient
import ng.bloodpleasure.ui.webview.BpWebViewClient
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

/**
 * Created by Ng on 16/04/2018.
 */
class MainActivityUi(
    private val bpJsInterface: BpJsInterface,
    private val bpWebViewClient: BpWebViewClient,
    private val bpWebChromeClient: BpWebChromeClient
) : AnkoComponent<MainActivity> {

    lateinit var webView: WebView

    @SuppressLint("JavascriptInterface")
    override fun createView(ui: AnkoContext<MainActivity>): View = ui.run {

        relativeLayout {


            button("重连") {
                onClick { ui.owner.connect() }
            }.lparams {
                width = wrapContent
                height = wrapContent
                alignParentBottom()
                centerHorizontally()
            }

            webView = webView {
                webViewClient = bpWebViewClient
                webChromeClient = bpWebChromeClient
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.loadsImagesAutomatically = true
                settings.defaultTextEncodingName = "utf-8"
                settings.saveFormData = false
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.setSupportZoom(false)
                removeJavascriptInterface("searchBoxJavaBridge_")
                removeJavascriptInterface("accessibility")
                removeJavascriptInterface("accessibilityTraversal")
                addJavascriptInterface(bpJsInterface, BpJsInterface.JS_CLASS_NAME)

            }.lparams(matchParent, matchParent)

        }


    }


    fun jsStatus(enabled: Boolean): MainActivityUi = apply {
        webView.settings.javaScriptEnabled = enabled
    }
}