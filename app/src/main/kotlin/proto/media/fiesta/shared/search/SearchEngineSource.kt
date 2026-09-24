package proto.media.fiesta.shared.search

import android.content.Context
import proto.media.fiesta.support.search.SearchEngineCatalog
import proto.media.fiesta.support.search.SearchEngineSelector

class SearchEngineSource(context: Context) {

    private val files = AssetSearchEngineFiles(context)

    private val selector: SearchEngineSelector by lazy { load() }

    fun selector(): SearchEngineSelector = selector

    private fun load(): SearchEngineSelector =
        SearchEngineCatalog.load(files).also { selector ->
            selector.engines.forEach { SearchEngineTextsById.forId(it.id) }
        }
}
