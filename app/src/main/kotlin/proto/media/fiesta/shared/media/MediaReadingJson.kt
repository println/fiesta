package proto.media.fiesta.shared.media

import org.json.JSONObject
import proto.media.fiesta.support.media.dto.MediaReadingDto

object MediaReadingJson {

    fun parse(json: String): MediaReadingDto? = try {
        val obj = JSONObject(json)
        val empty = MediaReadingDto.EMPTY
        MediaReadingDto(
            hasMedia = obj.optBoolean("hasMedia", empty.hasMedia),
            playing = obj.optBoolean("playing", empty.playing),
            positionSeconds = obj.optDouble("positionSeconds", empty.positionSeconds),
            durationSeconds = obj.optDouble("durationSeconds", empty.durationSeconds),
            playbackRate = obj.optDouble("playbackRate", empty.playbackRate),
            title = obj.optString("title", empty.title),
            artist = obj.optString("artist", empty.artist),
            artworkUrl = obj.optString("artworkUrl", empty.artworkUrl),
            canSkipNext = obj.optBoolean("canSkipNext", empty.canSkipNext),
            canSkipPrevious = obj.optBoolean("canSkipPrevious", empty.canSkipPrevious),
            trackId = obj.optString("trackId", empty.trackId),
            pageUrl = obj.optString("pageUrl", empty.pageUrl)
        )
    } catch (e: Exception) {
        null
    }
}
