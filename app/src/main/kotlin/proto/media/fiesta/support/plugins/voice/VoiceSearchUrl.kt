package proto.media.fiesta.support.plugins.voice

import java.net.URLEncoder

object VoiceSearchUrl {

    const val SEARCH_TERMS = "{searchTerms}"

    fun isValidResource(resource: String): Boolean {
        if (!resource.startsWith("/")) return false
        val marker = resource.indexOf(SEARCH_TERMS)
        if (marker < 0) return false
        val rest = resource.removeRange(marker, marker + SEARCH_TERMS.length)
        return '{' !in rest && '}' !in rest
    }

    fun of(domain: String, resource: String, query: String): String =
        "https://$domain" + resource.replace(SEARCH_TERMS, URLEncoder.encode(query, "UTF-8"))
}
