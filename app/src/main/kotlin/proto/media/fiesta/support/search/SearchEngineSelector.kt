package proto.media.fiesta.support.search

import java.net.MalformedURLException
import java.net.URL

class SearchEngineSelector(
    val engines: List<SearchEngine>,
    val fallback: SearchEngine
) {

    fun forUrl(url: String?): SearchEngine {
        val host = hostOf(url) ?: return fallback
        return engines.firstOrNull { it.handles(host) } ?: fallback
    }

    private fun hostOf(url: String?): String? {
        if (url.isNullOrBlank()) return null
        return try {
            URL(url).host?.takeIf { it.isNotEmpty() }
        } catch (e: MalformedURLException) {
            null
        }
    }
}
