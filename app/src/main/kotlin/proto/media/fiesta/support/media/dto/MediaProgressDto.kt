package proto.media.fiesta.support.media.dto

data class MediaProgressDto(
    val positionMillis: Long,
    val durationMillis: Long,
    val speed: Float,
    val updatedAtMillis: Long
) {
    companion object {
        val NONE = MediaProgressDto(
            positionMillis = 0L,
            durationMillis = 0L,
            speed = 0f,
            updatedAtMillis = 0L
        )
    }
}
