package proto.media.fiesta.shared.search

import proto.media.fiesta.R
import proto.media.fiesta.support.search.SearchEngine

data class SearchEngineTexts(
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
            hintRes = R.string.car_search_hint_google,
            titleRes = R.string.car_search_title_google,
            listeningRes = R.string.car_voice_listening_google,
            iconRes = R.drawable.ic_search_engine_google
        )
        "youtube" -> SearchEngineTexts(
            hintRes = R.string.car_search_hint_youtube,
            titleRes = R.string.car_search_title_youtube,
            listeningRes = R.string.car_voice_listening_youtube,
            iconRes = R.drawable.youtube
        )
        else -> throw IllegalStateException("no texts for search engine: $id")
    }
}
