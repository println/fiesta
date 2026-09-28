package proto.media.fiesta.support.media.dto

import proto.media.fiesta.support.media.config.MediaDefaults

data class MediaReadingDto(
    val hasMedia: Boolean,
    val playing: Boolean,
    val positionSeconds: Double,
    val durationSeconds: Double,
    val playbackRate: Double,
    val title: String,
    val artist: String,
    val artworkUrl: String,
    val canSkipNext: Boolean,
    val canSkipPrevious: Boolean,
    val trackId: String = "",
    val isTrack: Boolean = false,
    val pageUrl: String = ""
) {
    val trackIdentity: String
        get() = trackId.ifEmpty { listOf(pageUrl, title, artist).joinToString(MediaDefaults.TRACK_IDENTITY_SEPARATOR) }

    companion object {
        val EMPTY = MediaReadingDto(
            hasMedia = false,
            playing = false,
            positionSeconds = 0.0,
            durationSeconds = 0.0,
            playbackRate = 0.0,
            title = "",
            artist = "",
            artworkUrl = "",
            canSkipNext = false,
            canSkipPrevious = false
        )
    }
}
