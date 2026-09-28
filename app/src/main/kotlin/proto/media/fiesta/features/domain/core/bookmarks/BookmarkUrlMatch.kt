package proto.media.fiesta.features.domain.core.bookmarks

import java.net.URI

object BookmarkUrlMatch {

    fun sameUrl(a: String?, b: String?): Boolean {
        if (a == null || b == null) {
            return a == b
        }
        return normalize(a) == normalize(b)
    }

    private fun normalize(url: String): String {
        val withoutFragment = url.substringBefore('#')
        val uri = try {
            URI(withoutFragment)
        } catch (e: Exception) {
            return withoutFragment.trimEnd('/')
        }
        val scheme = "https"
        val host = uri.host?.lowercase()?.removePrefix("m.")?.removePrefix("www.")
        val path = (uri.path ?: "").trimEnd('/')
        val query = uri.query?.let { "?$it" } ?: ""
        return if (host == null) {
            withoutFragment.trimEnd('/')
        } else {
            "$scheme://$host$path$query"
        }
    }
}
