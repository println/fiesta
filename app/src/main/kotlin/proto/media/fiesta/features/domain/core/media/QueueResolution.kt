package proto.media.fiesta.features.domain.core.media

import proto.media.fiesta.support.media.dto.MediaQueueDto
import proto.media.fiesta.support.media.dto.QueueEntryDto
import proto.media.fiesta.support.media.dto.QueueShape

object QueueResolution {

    fun resolve(
        pageQueue: MediaQueueDto,
        historyQueue: MediaQueueDto,
        previousTrack: QueueEntryDto? = null
    ): MediaQueueDto {
        when (pageQueue.shape) {
            QueueShape.LIST -> if (pageQueue.entries.isNotEmpty()) return pageQueue
            QueueShape.STREAM -> if (pageQueue.entries.isNotEmpty()) {
                return withPreviousTrack(pageQueue, previousTrack)
            }
            QueueShape.NONE -> if (historyQueue.entries.isNotEmpty()) return historyQueue
        }
        return MediaQueueDto.EMPTY
    }

    private fun withPreviousTrack(pageQueue: MediaQueueDto, previousTrack: QueueEntryDto?): MediaQueueDto {
        if (previousTrack == null || pageQueue.cursor != 0) return pageQueue
        return pageQueue.copy(entries = listOf(previousTrack) + pageQueue.entries, cursor = 1)
    }
}
