package proto.media.fiesta.support.media.dto

data class RendererDto(val id: String, val availability: RendererAvailability) {

    companion object {
        val NONE = RendererDto(id = "", availability = RendererAvailability.ABSENT)
    }
}
