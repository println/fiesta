package proto.media.fiesta.support.webviewex.navigation

data class ResolvedAddress(val url: String, val schemeGuessed: Boolean, val isSearch: Boolean)

object AddressResolver {

    private val VALID_URL_PREFIXES = listOf(
        "file:///android_asset/", "file:///android_res/", "file:",
        "about:", "http:", "https:", "javascript:", "content:"
    )

    fun resolve(text: String, search: (String) -> String): String = resolveDetailed(text, search).url

    fun resolveDetailed(text: String, search: (String) -> String): ResolvedAddress {
        if (looksLikeSearchQuery(text)) return ResolvedAddress(search(text), schemeGuessed = false, isSearch = true)
        if (isValidUrl(text)) return ResolvedAddress(text, schemeGuessed = false, isSearch = false)
        val withScheme = "https://$text"
        return if (isValidUrl(withScheme)) {
            ResolvedAddress(withScheme, schemeGuessed = true, isSearch = false)
        } else {
            ResolvedAddress(search(text), schemeGuessed = false, isSearch = true)
        }
    }

    private fun isValidUrl(url: String): Boolean = VALID_URL_PREFIXES.any { url.startsWith(it) }

    private fun looksLikeSearchQuery(s: String): Boolean {
        val trimmed = s.trim()
        if (trimmed.isEmpty()) return false
        if (trimmed.contains(" ")) return true
        if (trimmed.contains(".") || trimmed.contains(":")) return false
        return !"localhost".equals(trimmed, ignoreCase = true)
    }
}
