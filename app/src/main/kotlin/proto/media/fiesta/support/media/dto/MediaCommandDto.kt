package proto.media.fiesta.support.media.dto

sealed interface MediaCommandDto {
    data object Play : MediaCommandDto
    data object Pause : MediaCommandDto
    data object Stop : MediaCommandDto
    data object SkipToNext : MediaCommandDto
    data object SkipToPrevious : MediaCommandDto
    data object FastForward : MediaCommandDto
    data object Rewind : MediaCommandDto
    data class SeekTo(val positionMillis: Long) : MediaCommandDto
    data class PlayFromSearch(val query: String) : MediaCommandDto
    data class SkipToQueueItem(val id: Long) : MediaCommandDto
}
