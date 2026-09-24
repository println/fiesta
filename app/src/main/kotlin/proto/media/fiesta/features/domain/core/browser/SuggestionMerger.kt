package proto.media.fiesta.features.domain.core.browser

object SuggestionMerger {

    enum class Type { INLINE_COMPLETION, HISTORY, BOOKMARK, SEARCH }

    data class Suggestion(val type: Type, val text: String, val url: String, val host: String?)

    fun merge(
        inlineCompletion: Suggestion?,
        history: List<Suggestion>,
        bookmarks: List<Suggestion>,
        searchSuggestions: List<Suggestion>,
        limit: Int
    ): List<Suggestion> {
        val result = mutableListOf<Suggestion>()
        val seenHosts = mutableSetOf<String>()

        fun addAll(suggestions: List<Suggestion>) {
            for (suggestion in suggestions) {
                if (result.size >= limit) {
                    return
                }
                val host = suggestion.host
                if (host != null && !seenHosts.add(host)) {
                    continue
                }
                result.add(suggestion)
            }
        }

        inlineCompletion?.let { addAll(listOf(it)) }
        addAll(history)
        addAll(bookmarks)
        addAll(searchSuggestions)

        return result.take(limit)
    }
}
