package proto.media.fiesta.support.webviewex.internal

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import proto.media.fiesta.support.webviewex.WebViewExConfig
import proto.media.fiesta.support.webviewex.WebViewExHost
import proto.media.fiesta.support.webviewex.gearhead.WebViewExGearhead
import proto.media.fiesta.support.webviewex.navigation.UserAgentPolicy

internal object BrowserSettings {

    @SuppressLint("SetJavaScriptEnabled")
    fun apply(view: WebView, config: WebViewExConfig, host: WebViewExHost, gearhead: WebViewExGearhead?) {
        with(view.settings) {
            javaScriptEnabled = true
            allowFileAccess = true
            domStorageEnabled = true
            allowFileAccessFromFileURLs = true
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false
            setSupportMultipleWindows(true)
            javaScriptCanOpenWindowsAutomatically = true
            if (config.allowUniversalFileAccess) allowUniversalAccessFromFileURLs = true
        }
        gearhead?.configure(view.settings)
        val cookies = CookieManager.getInstance()
        cookies.setAcceptCookie(true)
        cookies.setAcceptThirdPartyCookies(view, host.thirdPartyCookiesAllowed())
        WebView.setWebContentsDebuggingEnabled(host.isDebuggable())
    }

    fun applyUserAgent(context: Context, view: WebView, wantDesktop: Boolean, stripWebViewMarker: Boolean) {
        val desired = UserAgentPolicy.userAgentFor(wantDesktop, stripWebViewMarker, WebSettings.getDefaultUserAgent(context))
        if (view.settings.userAgentString != desired) view.settings.userAgentString = desired
    }
}
