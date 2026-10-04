package proto.media.fiezta.support.search

import java.io.InputStream

data class SearchEngineDefaults(val order: List<String>, val fallback: String)

interface SearchEngineFiles {

    fun readDefaults(): SearchEngineDefaults

    fun openDescription(engineId: String): InputStream
}
