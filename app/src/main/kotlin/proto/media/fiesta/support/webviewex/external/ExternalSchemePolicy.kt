package proto.media.fiesta.support.webviewex.external

enum class UrlKind { WEB, INTENT_URI, ANDROID_APP, MARKET, MAILTO, TEL, SMS, OTHER_EXTERNAL }

sealed class ExternalResolution {
    object Launch : ExternalResolution()
    data class LoadFallback(val url: String) : ExternalResolution()
    data class OpenMarket(val packageName: String) : ExternalResolution()
    object NotInstalled : ExternalResolution()
}

object ExternalSchemePolicy {

    private val WEB_SCHEMES = setOf("http", "https", "about", "file", "data", "javascript", "blob", "content", "ftp")

    private const val GRANT_FLAGS = 0x1 or 0x2 or 0x40 or 0x80
    private const val NEW_DOCUMENT_FLAG = 0x00080000

    fun classify(url: String): UrlKind {
        if (url.indexOf(':') < 0) return UrlKind.WEB
        return when (val scheme = url.substringBefore(':').lowercase()) {
            in WEB_SCHEMES -> UrlKind.WEB
            "intent" -> UrlKind.INTENT_URI
            "android-app" -> UrlKind.ANDROID_APP
            "market" -> UrlKind.MARKET
            "mailto" -> UrlKind.MAILTO
            "tel" -> UrlKind.TEL
            "sms", "smsto" -> UrlKind.SMS
            else -> if (scheme.isEmpty()) UrlKind.WEB else UrlKind.OTHER_EXTERNAL
        }
    }

    fun sanitizeFlags(flags: Int): Int = flags and (GRANT_FLAGS or NEW_DOCUMENT_FLAG).inv()

    fun isWebUrl(url: String): Boolean {
        val scheme = url.substringBefore(':', "").lowercase()
        return scheme == "http" || scheme == "https"
    }

    fun resolve(resolvesToOtherApp: Boolean, fallbackUrl: String?, declaredPackage: String?): ExternalResolution = when {
        resolvesToOtherApp -> ExternalResolution.Launch
        fallbackUrl != null && isWebUrl(fallbackUrl) -> ExternalResolution.LoadFallback(fallbackUrl)
        !declaredPackage.isNullOrBlank() -> ExternalResolution.OpenMarket(declaredPackage)
        else -> ExternalResolution.NotInstalled
    }
}
