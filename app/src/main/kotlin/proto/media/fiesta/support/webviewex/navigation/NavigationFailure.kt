package proto.media.fiesta.support.webviewex.navigation

sealed class NavigationFailure(val kind: String) {
    object Offline : NavigationFailure("offline")
    object DnsFailure : NavigationFailure("dns")
    object ConnectionRefused : NavigationFailure("connection")
    object Timeout : NavigationFailure("timeout")
    object SslError : NavigationFailure("ssl")
    object CleartextBlocked : NavigationFailure("cleartext")
    data class HttpStatus(val code: Int) : NavigationFailure("http")
    object UnsupportedScheme : NavigationFailure("scheme")
    object AppNotInstalled : NavigationFailure("app_missing")
    object BlockedByContentFilter : NavigationFailure("blocked")
    object Unknown : NavigationFailure("unknown")

    val isConnectionLevel: Boolean
        get() = this == DnsFailure || this == ConnectionRefused || this == SslError

    companion object {
        private const val ERROR_HOST_LOOKUP = -2
        private const val ERROR_CONNECT = -6
        private const val ERROR_TIMEOUT = -8
        private const val ERROR_UNSUPPORTED_SCHEME = -10
        private const val ERROR_FAILED_SSL_HANDSHAKE = -11

        fun classify(errorCode: Int, description: String, online: Boolean): NavigationFailure = when {
            "ERR_CLEARTEXT_NOT_PERMITTED" in description -> CleartextBlocked
            "ERR_BLOCKED_BY_CLIENT" in description -> BlockedByContentFilter
            "ERR_INTERNET_DISCONNECTED" in description -> Offline
            "ERR_CERT" in description || "ERR_SSL" in description || errorCode == ERROR_FAILED_SSL_HANDSHAKE -> SslError
            errorCode == ERROR_UNSUPPORTED_SCHEME -> UnsupportedScheme
            !online && (errorCode == ERROR_HOST_LOOKUP || errorCode == ERROR_CONNECT || errorCode == ERROR_TIMEOUT) -> Offline
            errorCode == ERROR_HOST_LOOKUP -> DnsFailure
            errorCode == ERROR_TIMEOUT -> Timeout
            errorCode == ERROR_CONNECT -> ConnectionRefused
            else -> Unknown
        }
    }
}
