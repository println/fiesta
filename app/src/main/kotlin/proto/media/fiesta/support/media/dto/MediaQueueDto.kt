package proto.media.fiesta.support.media.dto

import proto.media.fiesta.support.media.config.MediaDefaults

data class QueueEntryDto(val id: Long, val title: String, val subtitle: String, val iconUrl: String)

enum class QueueShape { LIST, STREAM, NONE }

data class MediaQueueDto(
    val title: String,
    val entries: List<QueueEntryDto>,
    val cursor: Int,
    val shape: QueueShape
) {
    val current: QueueEntryDto?
        get() = entries.getOrNull(cursor)

    fun window(radius: Int = MediaDefaults.QUEUE_WINDOW_RADIUS): MediaQueueDto {
        if (cursor <= MediaDefaults.UNKNOWN_CURSOR) return copy(entries = entries.take(2 * radius))
        val from = (cursor - radius).coerceAtLeast(0)
        val to = (cursor + radius).coerceAtMost(entries.size - 1)
        return copy(entries = entries.subList(from, to + 1), cursor = cursor - from)
    }

    companion object {
        val EMPTY = MediaQueueDto(title = "", entries = emptyList(), cursor = MediaDefaults.UNKNOWN_CURSOR, shape = QueueShape.NONE)
    }
}
