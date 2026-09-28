package proto.media.fiesta.support.media.dto

data class MediaStateDto(
    val playback: Playback,
    val renderer: RendererDto,
    val track: MediaTrackDto,
    val progress: MediaProgressDto,
    val capabilities: CapabilitiesDto,
    val queue: MediaQueueDto,
    val history: List<QueueEntryDto>,
    val audible: Boolean,
    val errorMessage: String?
) {
    enum class Playback { NONE, CONNECTING, BUFFERING, PLAYING, PAUSED, STOPPED, ERROR }

    companion object {
        val EMPTY = MediaStateDto(
            playback = Playback.NONE,
            renderer = RendererDto.NONE,
            track = MediaTrackDto.EMPTY,
            progress = MediaProgressDto.NONE,
            capabilities = CapabilitiesDto.NONE,
            queue = MediaQueueDto.EMPTY,
            history = emptyList(),
            audible = false,
            errorMessage = null
        )
    }
}
