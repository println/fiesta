package proto.media.fiesta.features.domain.car.player

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import proto.media.fiesta.features.domain.core.media.RecentlyPlayed
import proto.media.fiesta.support.media.dto.MediaReadingDto
import proto.media.fiesta.support.media.dto.QueueEntryDto

class RecentlyPlayedStore(context: Context) {

    private val prefs = context.getSharedPreferences(CarPlayerStateStore.PREFS, Context.MODE_PRIVATE)
    private val pageUrls = mutableMapOf<Long, String>()

    var entries: List<QueueEntryDto> = load()
        private set

    fun pageUrlFor(id: Long): String? = pageUrls[id]

    fun excluding(trackIdentity: String): List<QueueEntryDto> {
        val id = RecentlyPlayed.idFor(trackIdentity)
        return entries.filterNot { it.id == id }
    }

    fun record(reading: MediaReadingDto) {
        val id = RecentlyPlayed.idFor(reading.trackIdentity)
        pageUrls[id] = reading.pageUrl
        val entry = QueueEntryDto(id, reading.title, reading.artist, reading.artworkUrl)
        entries = RecentlyPlayed.append(entries, entry, RECENTLY_PLAYED_LIMIT)
        save()
    }

    private fun load(): List<QueueEntryDto> {
        val json = prefs.getString(KEY, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                val id = item.getLong("id")
                pageUrls[id] = item.optString("pageUrl", "")
                QueueEntryDto(
                    id = id,
                    title = item.optString("title", ""),
                    subtitle = item.optString("subtitle", ""),
                    iconUrl = item.optString("iconUrl", "")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun save() {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(
                JSONObject()
                    .put("id", entry.id)
                    .put("title", entry.title)
                    .put("subtitle", entry.subtitle)
                    .put("iconUrl", entry.iconUrl)
                    .put("pageUrl", pageUrls[entry.id] ?: "")
            )
        }
        prefs.edit().putString(KEY, array.toString()).apply()
    }

    companion object {
        const val RECENTLY_PLAYED_LIMIT = 20
        private const val KEY = "recently_played"
    }
}
