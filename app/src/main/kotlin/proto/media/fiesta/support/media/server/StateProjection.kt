package proto.media.fiesta.support.media.server

import proto.media.fiesta.support.media.config.MediaDefaults
import proto.media.fiesta.support.media.dto.CapabilitiesDto
import proto.media.fiesta.support.media.dto.MediaAction
import proto.media.fiesta.support.media.dto.MediaProgressDto
import proto.media.fiesta.support.media.dto.MediaQueueDto
import proto.media.fiesta.support.media.dto.MediaReadingDto
import proto.media.fiesta.support.media.dto.MediaStateDto
import proto.media.fiesta.support.media.dto.MediaTrackDto
import proto.media.fiesta.support.media.dto.QueueEntryDto
import proto.media.fiesta.support.media.dto.RendererAvailability
import proto.media.fiesta.support.media.dto.RendererDto

internal object StateProjection {

    fun project(
        reading: MediaReadingDto,
        queue: MediaQueueDto,
        history: List<QueueEntryDto>,
        echo: CommandEcho?,
        preparing: String?,
        error: String?,
        rendererId: String,
        availability: RendererAvailability,
        stopped: Boolean,
        interrupted: Boolean,
        nowMillis: Long
    ): MediaStateDto {
        val track = MediaTrackDto(
            identity = reading.trackIdentity,
            title = reading.title,
            artist = reading.artist,
            artworkUrl = reading.artworkUrl
        )
        val playing = MediaProgressDto(
            positionMillis = (reading.positionSeconds * MediaDefaults.MILLIS_PER_SECOND).toLong(),
            durationMillis = (reading.durationSeconds * MediaDefaults.MILLIS_PER_SECOND).toLong(),
            speed = if (reading.playing) reading.playbackRate.toFloat() else 0f,
            updatedAtMillis = nowMillis
        )
        val still = playing.copy(speed = 0f)
        val seekable = reading.durationSeconds > 0.0 &&
            availability != RendererAvailability.ABSENT &&
            preparing == null
        val queueActions = if (queue.entries.isEmpty()) {
            emptySet()
        } else {
            setOf(MediaAction.SKIP_TO_QUEUE_ITEM)
        }
        val restricted = CapabilitiesDto(MediaAction.ALWAYS_AVAILABLE + queueActions)
        val skips = buildSet {
            if (reading.canSkipNext) add(MediaAction.SKIP_TO_NEXT)
            if (reading.canSkipPrevious) add(MediaAction.SKIP_TO_PREVIOUS)
        }
        val withoutSeek = CapabilitiesDto(restricted.allowed + skips)
        val reported = if (seekable) {
            CapabilitiesDto(withoutSeek.allowed + MediaAction.SEEK_TO)
        } else {
            withoutSeek
        }
        val base = MediaStateDto(
            playback = MediaStateDto.Playback.NONE,
            renderer = RendererDto(rendererId, availability),
            track = track,
            progress = still,
            capabilities = withoutSeek,
            queue = queue,
            history = history,
            audible = reading.playing,
            errorMessage = null
        )

        if (error != null) {
            return base.copy(playback = MediaStateDto.Playback.ERROR, errorMessage = error)
        }
        if (preparing != null) {
            return base.copy(
                playback = MediaStateDto.Playback.CONNECTING,
                track = MediaTrackDto.EMPTY.copy(title = preparing),
                progress = MediaProgressDto.NONE.copy(updatedAtMillis = nowMillis),
                capabilities = restricted
            )
        }
        if (!reading.isTrack) {
            return base.copy(
                playback = MediaStateDto.Playback.STOPPED,
                track = MediaTrackDto.EMPTY,
                progress = MediaProgressDto.NONE.copy(updatedAtMillis = nowMillis),
                audible = false
            )
        }
        if (availability == RendererAvailability.STARTING) {
            return base.copy(
                playback = MediaStateDto.Playback.CONNECTING,
                capabilities = restricted
            )
        }
        if (echo != null && nowMillis < echo.expiresAtMillis) {
            return base.copy(
                playback = if (echo.expectsPlaying) {
                    MediaStateDto.Playback.PLAYING
                } else {
                    MediaStateDto.Playback.PAUSED
                },
                progress = if (echo.expectsPlaying) playing else still,
                capabilities = reported
            )
        }
        if (stopped) {
            return base.copy(playback = MediaStateDto.Playback.STOPPED)
        }
        if (interrupted && availability != RendererAvailability.ABSENT) {
            return base.copy(
                playback = MediaStateDto.Playback.CONNECTING,
                capabilities = reported
            )
        }
        if (availability == RendererAvailability.ABSENT) {
            return base.copy(
                playback = if (track.hasMetadata) {
                    MediaStateDto.Playback.PAUSED
                } else {
                    MediaStateDto.Playback.NONE
                }
            )
        }
        if (availability == RendererAvailability.LOADING) {
            return base.copy(
                playback = MediaStateDto.Playback.BUFFERING,
                capabilities = reported
            )
        }
        if (reading.hasMedia) {
            return base.copy(
                playback = if (reading.playing) {
                    MediaStateDto.Playback.PLAYING
                } else {
                    MediaStateDto.Playback.PAUSED
                },
                progress = playing,
                capabilities = reported
            )
        }
        return base.copy(
            playback = MediaStateDto.Playback.PLAYING,
            progress = MediaProgressDto.NONE.copy(updatedAtMillis = nowMillis)
        )
    }
}
