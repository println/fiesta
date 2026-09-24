package proto.media.fiesta.support.media.dto

data class CapabilitiesDto(val allowed: Set<MediaAction>) {

    operator fun contains(action: MediaAction): Boolean = action in allowed

    companion object {
        val NONE = CapabilitiesDto(emptySet())
    }
}
