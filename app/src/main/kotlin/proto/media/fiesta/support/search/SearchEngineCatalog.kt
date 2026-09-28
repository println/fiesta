package proto.media.fiesta.support.search

object SearchEngineCatalog {

    fun load(files: SearchEngineFiles): SearchEngineSelector {
        val defaults = files.readDefaults()
        val engines = defaults.order.map { loadEngine(files, it) }
        val fallback = engines.firstOrNull { it.id == defaults.fallback }
            ?: throw IllegalStateException("fallback search engine not in order: ${defaults.fallback}")
        return SearchEngineSelector(engines, fallback)
    }

    private fun loadEngine(files: SearchEngineFiles, id: String): DescribedSearchEngine {
        val description = files.openDescription(id).use { OpenSearchParser.parse(it) }
        if (description.id != id) {
            throw IllegalStateException("search engine file $id.xml declares id ${description.id}")
        }
        return DescribedSearchEngine(description)
    }
}
