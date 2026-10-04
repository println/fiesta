package proto.media.fiezta.support.media.dto

sealed interface MediaEventDto {
    data class StateChanged(val state: MediaStateDto) : MediaEventDto
}
