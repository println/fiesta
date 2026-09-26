package proto.media.fiesta.features.domain.core.media

import proto.media.fiesta.support.media.dto.MediaReadingDto
import proto.media.fiesta.support.media.dto.QueueEntryDto

object RecentlyPlayed {

    private const val MINIMUM_SECONDS = 30.0
    private const val MINIMUM_FRACTION = 0.25

    fun idFor(trackIdentity: String): Long =
        -(trackIdentity.hashCode().toLong() and 0x7fffffffL) - 2

    fun shouldRecord(reading: MediaReadingDto, lastRecordedIdentity: String): Boolean =
        reading.isTrack &&
            reading.trackIdentity != lastRecordedIdentity &&
            isWorthRecording(reading.positionSeconds, reading.durationSeconds)

    fun isWorthRecording(positionSeconds: Double, durationSeconds: Double): Boolean =
        positionSeconds >= thresholdFor(durationSeconds)

    private fun thresholdFor(durationSeconds: Double): Double {
        if (durationSeconds <= 0) return MINIMUM_SECONDS
        return minOf(MINIMUM_SECONDS, durationSeconds * MINIMUM_FRACTION)
    }

    fun append(entries: List<QueueEntryDto>, entry: QueueEntryDto, limit: Int): List<QueueEntryDto> {
        val withoutDuplicate = entries.filterNot { it.id == entry.id }
        return (withoutDuplicate + entry).takeLast(limit)
    }
}
