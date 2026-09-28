package proto.media.fiesta.support.media.dto

sealed interface MediaEventDto {
    data class StateChanged(val state: MediaStateDto) : MediaEventDto
}
