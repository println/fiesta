package proto.media.fiesta.support.search

interface SearchEngine {
    val id: String

    fun handles(host: String): Boolean

    fun searchUrl(query: String): String

    fun suggestionsUrl(query: String): String?
}
