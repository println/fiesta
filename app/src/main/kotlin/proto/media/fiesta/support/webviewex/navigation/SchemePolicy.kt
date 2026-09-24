package proto.media.fiesta.support.webviewex.navigation

enum class Scheme(val prefix: String) {
    HTTPS("https://"),
    HTTP("http://");

    companion object {
        fun of(url: String): Scheme? = values().firstOrNull { url.startsWith(it.prefix, ignoreCase = true) }
    }
}

object SchemePolicy {

    fun initialUrl(address: ResolvedAddress, remembered: Scheme?): String {
        if (!address.schemeGuessed || remembered != Scheme.HTTP) return address.url
        return Scheme.HTTP.prefix + address.url.removePrefix(Scheme.HTTPS.prefix)
    }

    fun fallbackUrl(transaction: NavigationTransaction, failure: NavigationFailure, pinnedToHttps: Boolean): String? {
        if (!transaction.schemeGuessed || transaction.fellBackToHttp || pinnedToHttps) return null
        if (!failure.isConnectionLevel) return null
        val url = transaction.requestedUrl
        if (Scheme.of(url) != Scheme.HTTPS) return null
        return Scheme.HTTP.prefix + url.substring(Scheme.HTTPS.prefix.length)
    }

    fun schemeToRemember(transaction: NavigationTransaction, finalUrl: String): Scheme? {
        if (!transaction.schemeGuessed) return null
        return Scheme.of(finalUrl)
    }

    fun isInsecure(url: String): Boolean = Scheme.of(url) == Scheme.HTTP
}
