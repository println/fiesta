package proto.media.fiesta.support.webviewex

interface WebViewExHost {
    fun prefersDesktop(host: String?): Boolean
    fun stripWebViewMarker(): Boolean
    fun thirdPartyCookiesAllowed(): Boolean
    fun isDebuggable(): Boolean = false
    fun isRequestBlocked(url: String, pageHost: String?): Boolean = false
    fun onRequestBlocked(url: String) = Unit
}
