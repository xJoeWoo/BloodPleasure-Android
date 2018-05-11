package ng.bloodpleasure.ui

import android.annotation.SuppressLint
import android.view.View
import android.webkit.WebView
import android.widget.LinearLayout
import ng.bloodpleasure.MainActivity
import ng.bloodpleasure.ui.webview.BpJsInterface
import ng.bloodpleasure.ui.webview.BpWebChromeClient
import ng.bloodpleasure.ui.webview.BpWebViewClient
import ng.bloodpleasure.util.statusBarHeight
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
            fitsSystemWindows = true

            WebView.setWebContentsDebuggingEnabled(true)

            webView = webView {
                webViewClient = bpWebViewClient
                webChromeClient = bpWebChromeClient
                settings.loadsImagesAutomatically = true
                settings.defaultTextEncodingName = "utf-8"
                settings.saveFormData = false
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.domStorageEnabled = true
                settings.databaseEnabled = true
                removeJavascriptInterface("searchBoxJavaBridge_")
                removeJavascriptInterface("accessibility")
                removeJavascriptInterface("accessibilityTraversal")
                addJavascriptInterface(bpJsInterface, BpJsInterface.JS_CLASS_NAME)
            }.lparams(matchParent, matchParent)

            relativeLayout {
                topPadding = ui.owner.statusBarHeight

                linearLayout {
                    orientation = LinearLayout.HORIZONTAL

                    button("重连") {
                        onClick { ui.owner.connect() }
                    }

                    button("刷新") {
                        onClick { webView.reload() }
                    }.lparams { leftMargin = 16 }
                }.lparams {
                    alignParentBottom()
                    centerHorizontally()
                }

            }.lparams(matchParent, matchParent)
        }
    }


    fun jsStatus(enabled: Boolean): MainActivityUi = apply {
        webView.settings.javaScriptEnabled = enabled
    }
}