package proto.media.fiezta.support.media.dto

data class MediaSnapshotDto(
    val track: MediaTrackDto,
    val positionMillis: Long,
    val durationMillis: Long,
    val queue: MediaQueueDto
)
