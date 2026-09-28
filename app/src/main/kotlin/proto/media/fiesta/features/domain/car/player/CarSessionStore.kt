package proto.media.fiesta.features.domain.car.player

import android.content.Context
import proto.media.fiesta.support.media.contract.SessionStore
import proto.media.fiesta.support.media.dto.MediaQueueDto
import proto.media.fiesta.support.media.dto.MediaSnapshotDto
import proto.media.fiesta.support.media.dto.MediaTrackDto

class CarSessionStore(context: Context) : SessionStore {

    private val prefs = context.getSharedPreferences(CarPlayerStateStore.PREFS, Context.MODE_PRIVATE)

    override fun load(rendererId: String): MediaSnapshotDto? {
        val title = prefs.getString(key(rendererId, TITLE), null) ?: return null
        return MediaSnapshotDto(
            track = MediaTrackDto(
                identity = prefs.getString(key(rendererId, IDENTITY), "")!!,
                title = title,
                artist = prefs.getString(key(rendererId, ARTIST), "")!!,
                artworkUrl = prefs.getString(key(rendererId, ARTWORK), "")!!
            ),
            positionMillis = prefs.getLong(key(rendererId, POSITION), 0L),
            durationMillis = prefs.getLong(key(rendererId, DURATION), 0L),
            queue = MediaQueueDto.EMPTY
        )
    }

    override fun save(rendererId: String, snapshot: MediaSnapshotDto) {
        prefs.edit()
            .putString(key(rendererId, TITLE), snapshot.track.title)
            .putString(key(rendererId, IDENTITY), snapshot.track.identity)
            .putString(key(rendererId, ARTIST), snapshot.track.artist)
            .putString(key(rendererId, ARTWORK), snapshot.track.artworkUrl)
            .putLong(key(rendererId, POSITION), snapshot.positionMillis)
            .putLong(key(rendererId, DURATION), snapshot.durationMillis)
            .apply()
    }

    override fun clear(rendererId: String) {
        prefs.edit()
            .remove(key(rendererId, TITLE))
            .remove(key(rendererId, IDENTITY))
            .remove(key(rendererId, ARTIST))
            .remove(key(rendererId, ARTWORK))
            .remove(key(rendererId, POSITION))
            .remove(key(rendererId, DURATION))
            .apply()
    }

    private fun key(rendererId: String, field: String) = "session.$rendererId.$field"

    private companion object {
        const val TITLE = "title"
        const val IDENTITY = "identity"
        const val ARTIST = "artist"
        const val ARTWORK = "artwork"
        const val POSITION = "position"
        const val DURATION = "duration"
    }
}
