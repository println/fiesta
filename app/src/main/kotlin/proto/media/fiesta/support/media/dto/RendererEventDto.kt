package proto.media.fiesta.support.media.dto

sealed interface RendererEventDto {
    data class Read(val reading: MediaReadingDto) : RendererEventDto
    data class QueueRead(val queue: MediaQueueDto) : RendererEventDto
    data class HistoryRead(val entries: List<QueueEntryDto>) : RendererEventDto
    data class AvailabilityChanged(val availability: RendererAvailability) : RendererEventDto
    data class Preparing(val label: String?) : RendererEventDto
    data class Failed(val message: String?) : RendererEventDto
    data class Interrupted(val interrupted: Boolean) : RendererEventDto
}
