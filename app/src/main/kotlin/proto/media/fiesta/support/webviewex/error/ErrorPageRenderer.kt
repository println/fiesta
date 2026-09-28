package proto.media.fiesta.support.webviewex.error

import android.content.Context
import android.os.Handler
import android.util.Log
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import org.json.JSONArray
import org.json.JSONObject
import proto.media.fiesta.support.webviewex.ErrorAction
import proto.media.fiesta.support.webviewex.WebViewExTexts
import proto.media.fiesta.support.webviewex.WebViewExTheme
import proto.media.fiesta.support.webviewex.navigation.NavigationFailure
import java.util.UUID

internal class ErrorPageRenderer(
    private val context: Context,
    private val texts: WebViewExTexts,
    private val style: ErrorPageStyle,
    private val onAction: (ErrorAction) -> Unit
) {

    data class Recovery(val attempt: Int, val seconds: Int, val waitingForNetwork: Boolean)

    private val template: String by lazy {
        context.assets.open(TEMPLATE_ASSET).bufferedReader().use { it.readText() }
    }
    private val handler = Handler(Looper.getMainLooper())
    private var token: String? = null

    val bridge = Bridge()

    fun render(
        webView: WebView,
        failingUrl: String,
        failure: NavigationFailure,
        offersHttpRetry: Boolean,
        details: List<String>,
        recovery: Recovery?
    ) {
        val newToken = UUID.randomUUID().toString()
        token = newToken
        val payload = JSONObject()
            .put("token", newToken)
            .put("style", style.name.lowercase())
            .put("theme", themeJson(texts.theme()))
            .put("failure", failure.kind)
            .put("title", texts.title(failure))
            .put("message", texts.message(failure))
            .put("url", failingUrl)
            .put("actions", actionsJson(offersHttpRetry, recovery != null))
            .put("status", statusFor(recovery))
            .put("detailsLabel", texts.detailsLabel())
            .put("details", JSONArray(if (style == ErrorPageStyle.DETAILED) details else emptyList<String>()))
        Log.d(TAG, "error page for $failingUrl: ${failure.kind} recovery=${recovery != null}")
        val html = template.replace(PAYLOAD_PLACEHOLDER, escapeForScriptTag(payload.toString()))
        webView.loadDataWithBaseURL(failingUrl, html, "text/html", "UTF-8", failingUrl)
    }

    fun updateRecovery(webView: WebView, recovery: Recovery?, offersHttpRetry: Boolean) {
        if (token == null) return
        val update = JSONObject()
            .put("status", statusFor(recovery))
            .put("actions", actionsJson(offersHttpRetry, recovery != null))
        webView.evaluateJavascript(
            "window.__webviewexError && window.__webviewexError.update(${update});",
            null
        )
    }

    fun invalidate() {
        token = null
    }

    private fun themeJson(theme: WebViewExTheme): JSONObject = JSONObject()
        .put("background", css(theme.background))
        .put("surface", css(theme.surface))
        .put("text", css(theme.text))
        .put("mutedText", css(theme.mutedText))
        .put("accent", css(theme.accent))
        .put("accentText", css(theme.accentText))
        .put("secondaryAccent", css(theme.secondaryAccent))
        .put("statusText", css(theme.statusText))
        .put("iconTint", css(theme.iconTint))

    private fun css(color: Int): String = String.format("#%06X", color and 0xFFFFFF)

    private fun statusFor(recovery: Recovery?): String = when {
        recovery == null -> ""
        recovery.waitingForNetwork -> texts.waitingForNetwork()
        style == ErrorPageStyle.COMPACT -> texts.reconnecting()
        else -> texts.recovering(recovery.attempt, recovery.seconds)
    }

    private fun actionsJson(offersHttpRetry: Boolean, recovering: Boolean): JSONArray {
        val actions = JSONArray()
        fun add(action: ErrorAction, primary: Boolean = false) {
            actions.put(
                JSONObject().put("id", action.id).put("label", texts.actionLabel(action)).put("primary", primary)
            )
        }
        add(ErrorAction.RETRY, primary = true)
        if (style == ErrorPageStyle.COMPACT) {
            add(ErrorAction.GO_BACK)
            return actions
        }
        if (offersHttpRetry) add(ErrorAction.RETRY_HTTP)
        add(ErrorAction.OPEN_EXTERNALLY)
        if (recovering) add(ErrorAction.CANCEL_RECOVERY)
        return actions
    }

    private fun escapeForScriptTag(json: String): String =
        json.replace("<", "\\u003c").replace("\u2028", "\\u2028").replace("\u2029", "\\u2029")

    inner class Bridge {
        @JavascriptInterface
        fun perform(callerToken: String, actionId: String) {
            val current = token
            if (current == null || current != callerToken) {
                Log.w(TAG, "rejected error bridge call from a stale or foreign document")
                return
            }
            val action = ErrorAction.values().firstOrNull { it.id == actionId } ?: return
            handler.post { if (token == callerToken) onAction(action) }
        }
    }

    companion object {
        const val BRIDGE_NAME = "webviewex"
        private const val TAG = "WebViewEx"
        const val TITLE_MARKER = "webviewex-error:"
        private const val TEMPLATE_ASSET = "webviewex/error.html"
        private const val PAYLOAD_PLACEHOLDER = "__PAYLOAD__"
    }
}
