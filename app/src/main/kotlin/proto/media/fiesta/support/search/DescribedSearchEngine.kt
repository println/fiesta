package proto.media.fiesta.support.search

class DescribedSearchEngine(val description: SearchEngineDescription) : SearchEngine {

    override val id: String = description.id

    override fun handles(host: String): Boolean =
        description.hosts.any { HostPattern.matches(it, host) }

    override fun searchUrl(query: String): String = fill(description.searchTemplate, query)

    override fun suggestionsUrl(query: String): String? =
        description.suggestionsTemplate?.let { fill(it, query) }

    private fun fill(template: String, query: String): String =
        template.replace(SEARCH_TERMS, encodeQuery(query))

    companion object {
        const val SEARCH_TERMS = "{searchTerms}"
    }
}
