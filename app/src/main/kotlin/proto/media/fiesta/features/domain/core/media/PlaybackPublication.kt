package proto.media.fiesta.features.domain.core.media

import kotlin.math.abs

data class PublishedPlayback(
    val playbackState: Int,
    val actions: Long,
    val activeQueueId: Long,
    val errorMessage: String,
    val positionMillis: Long,
    val speed: Float,
    val updatedAtMillis: Long
)

object PlaybackPublication {

    const val POSITION_DRIFT_TOLERANCE_MS = 2_000L

    fun worthPublishing(published: PublishedPlayback?, next: PublishedPlayback): Boolean {
        if (published == null) return true
        if (published.playbackState != next.playbackState ||
            published.actions != next.actions ||
            published.activeQueueId != next.activeQueueId ||
            published.errorMessage != next.errorMessage ||
            published.speed != next.speed
        ) {
            return true
        }
        val elapsed = next.updatedAtMillis - published.updatedAtMillis
        val extrapolated = published.positionMillis + (elapsed * published.speed).toLong()
        return abs(next.positionMillis - extrapolated) > POSITION_DRIFT_TOLERANCE_MS
    }
}
