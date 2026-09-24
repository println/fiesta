package proto.media.fiesta.support.webviewex

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.webkit.WebChromeClient
import android.webkit.WebView
import proto.media.fiesta.support.webviewex.gearhead.KeepsPageVisible

open class VideoWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = android.R.attr.webViewStyle
) : WebView(context, attrs, defStyle), KeepsPageVisible {

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setRendererPriorityPolicy(RENDERER_PRIORITY_IMPORTANT, false)
        }
    }

    private var videoEnabledWebChromeClient: WebViewExChromeClient? = null
    private var addedJavascriptInterface = false
    private var lastDownY = 0f
    private var lastDownX = 0f

    override var keepPageVisible = false

    var carInputHooks: ((WebView) -> Unit)? = null

    override fun onPause() {
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        if (keepPageVisible && visibility != VISIBLE) {
            super.onWindowVisibilityChanged(VISIBLE)
        } else {
            super.onWindowVisibilityChanged(visibility)
        }
    }

    fun isVideoFullscreen(): Boolean = videoEnabledWebChromeClient?.isVideoFullscreen() == true

    fun exitFullScreen() {
        videoEnabledWebChromeClient?.onHideCustomView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun setWebChromeClient(client: WebChromeClient?) {
        settings.javaScriptEnabled = true

        if (client is WebViewExChromeClient) {
            this.videoEnabledWebChromeClient = client
        }

        super.setWebChromeClient(client)
    }

    override fun loadData(data: String, mimeType: String?, encoding: String?) {
        addJavascriptInterfaceOnce()
        super.loadData(data, mimeType, encoding)
    }

    override fun loadDataWithBaseURL(
        baseUrl: String?, data: String, mimeType: String?, encoding: String?, historyUrl: String?
    ) {
        addJavascriptInterfaceOnce()
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
    }

    override fun loadUrl(url: String) {
        addJavascriptInterfaceOnce()
        super.loadUrl(url)
    }

    override fun loadUrl(url: String, additionalHttpHeaders: Map<String, String>) {
        addJavascriptInterfaceOnce()
        super.loadUrl(url, additionalHttpHeaders)
    }

    private fun addJavascriptInterfaceOnce() {
        if (!addedJavascriptInterface) {
            addJavascriptInterface(JavascriptInterface(), "_VideoEnabledWebView")
            addedJavascriptInterface = true
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            carInputHooks?.invoke(this)
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            lastDownX = event.x
            lastDownY = event.y
        }
        if (event.action == MotionEvent.ACTION_UP) {
            if (Math.abs(lastDownX - event.x) < TOUCH_TOLERANCE && Math.abs(lastDownY - event.y) < TOUCH_TOLERANCE) {
                carInputHooks?.invoke(this)
            }
        }
        return super.onTouchEvent(event)
    }

    inner class JavascriptInterface {
        @android.webkit.JavascriptInterface
        fun notifyVideoEnd() {
            Handler(Looper.getMainLooper()).post {
                videoEnabledWebChromeClient?.onHideCustomView()
            }
        }
    }

    companion object {
        private const val TOUCH_TOLERANCE = 20f
    }
}
