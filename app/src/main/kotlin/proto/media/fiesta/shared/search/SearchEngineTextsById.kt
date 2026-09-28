package proto.media.fiesta.shared.search

import proto.media.fiesta.R
import proto.media.fiesta.support.search.SearchEngine

data class SearchEngineTexts(
    val nameRes: Int,
    val hintRes: Int,
    val titleRes: Int,
    val listeningRes: Int,
    val iconRes: Int
)

val SearchEngine.texts: SearchEngineTexts
    get() = SearchEngineTextsById.forId(id)

object SearchEngineTextsById {

    fun forId(id: String): SearchEngineTexts = when (id) {
        "google" -> SearchEngineTexts(
            nameRes = R.string.car_search_engine_google,
            hintRes = R.string.car_search_hint_google,
            titleRes = R.string.car_search_title_google,
            listeningRes = R.string.car_voice_listening_google,
            iconRes = R.drawable.ic_search_engine_google
        )
        "youtube" -> SearchEngineTexts(
            nameRes = R.string.car_search_engine_youtube,
            hintRes = R.string.car_search_hint_youtube,
            titleRes = R.string.car_search_title_youtube,
            listeningRes = R.string.car_voice_listening_youtube,
            iconRes = R.drawable.youtube
        )
        "bing" -> SearchEngineTexts(
            nameRes = R.string.car_search_engine_bing,
            hintRes = R.string.car_search_hint_bing,
            titleRes = R.string.car_search_title_bing,
            listeningRes = R.string.car_voice_listening_bing,
            iconRes = R.drawable.ic_search_engine_bing
        )
        "duckduckgo" -> SearchEngineTexts(
            nameRes = R.string.car_search_engine_duckduckgo,
            hintRes = R.string.car_search_hint_duckduckgo,
            titleRes = R.string.car_search_title_duckduckgo,
            listeningRes = R.string.car_voice_listening_duckduckgo,
            iconRes = R.drawable.ic_search_engine_duckduckgo
        )
        else -> throw IllegalStateException("no texts for search engine: $id")
    }
}
