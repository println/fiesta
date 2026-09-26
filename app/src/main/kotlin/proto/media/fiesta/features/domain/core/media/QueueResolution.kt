package proto.media.fiesta.features.domain.core.media

import proto.media.fiesta.support.media.config.MediaDefaults
import proto.media.fiesta.support.media.dto.MediaQueueDto
import proto.media.fiesta.support.media.dto.QueueEntryDto
import proto.media.fiesta.support.media.dto.QueueShape
import proto.media.fiesta.support.webviewex.BrowserHistory

object QueueResolution {

    fun toMediaOnly(history: BrowserHistory, mediaUrls: Set<String>): BrowserHistory {
        val currentUrl = history.entries.getOrNull(history.currentPosition)?.url
        val kept = history.entries.filter { it.url in mediaUrls }
        val position = kept.indexOfFirst { it.url == currentUrl }
        return BrowserHistory(kept, if (position >= 0) position else MediaDefaults.UNKNOWN_CURSOR)
    }

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
