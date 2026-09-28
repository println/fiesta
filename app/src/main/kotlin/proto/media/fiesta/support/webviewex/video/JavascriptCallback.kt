package proto.media.fiesta.support.webviewex.video

import android.os.Handler
import android.webkit.JavascriptInterface

class JavascriptCallback(private val callbacks: JSCallbacks) {

    private val handler = Handler()

    @JavascriptInterface
    fun onVideoDiscovered() {
        handler.post { callbacks.onVideoElementDiscovered() }
    }

    @JavascriptInterface
    fun onFullScreenUnavailable() {
        handler.post { callbacks.onFullScreenUnavailable() }
    }

    @JavascriptInterface
    fun showKeyboard(oldText: String) {
        handler.post { callbacks.onShowKeyboardFromJS(oldText) }
    }

    @JavascriptInterface
    fun hideKeyboard() {
        handler.post { callbacks.onHideKeyboardFromJS() }
    }

    interface JSCallbacks {
        fun onVideoElementDiscovered()

        fun onFullScreenUnavailable()

        fun onShowKeyboardFromJS(oldText: String)

        fun onHideKeyboardFromJS()
    }
}
