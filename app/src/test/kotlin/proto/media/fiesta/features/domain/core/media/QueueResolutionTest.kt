package proto.media.fiesta.features.domain.core.media

import proto.media.fiesta.support.media.dto.MediaQueueDto
import proto.media.fiesta.support.media.dto.QueueEntryDto
import proto.media.fiesta.support.media.dto.QueueShape
import proto.media.fiesta.support.webviewex.BrowserHistory
import proto.media.fiesta.support.webviewex.HistoryEntry

import org.junit.Assert.assertEquals
import org.junit.Test

class QueueResolutionTest {

    private fun entry(id: Long) = QueueEntryDto(id, "title$id", "subtitle$id", "")

    private fun historyEntry(index: Int, url: String) = HistoryEntry(rawIndex = index, url = url, title = "title$index")

    @Test
    fun `only the entries seen as media are kept`() {
        val history = BrowserHistory(
            entries = listOf(historyEntry(0, "https://plain.example/"), historyEntry(1, "https://media.example/")),
            currentPosition = 1
        )

        val filtered = QueueResolution.toMediaOnly(history, mediaUrls = setOf("https://media.example/"))

        assertEquals(listOf("https://media.example/"), filtered.entries.map { it.url })
        assertEquals(0, filtered.currentPosition)
    }

    @Test
    fun `the cursor is unknown when the current page is not media`() {
        val history = BrowserHistory(
            entries = listOf(historyEntry(0, "https://media.example/"), historyEntry(1, "https://plain.example/")),
            currentPosition = 1
        )

        val filtered = QueueResolution.toMediaOnly(history, mediaUrls = setOf("https://media.example/"))

        assertEquals(listOf("https://media.example/"), filtered.entries.map { it.url })
        assertEquals(-1, filtered.currentPosition)
    }

    @Test
    fun `with nothing ever seen as media the history is empty`() {
        val history = BrowserHistory(entries = listOf(historyEntry(0, "https://plain.example/")), currentPosition = 0)

        val filtered = QueueResolution.toMediaOnly(history, mediaUrls = emptySet())

        assertEquals(emptyList<HistoryEntry>(), filtered.entries)
        assertEquals(-1, filtered.currentPosition)
    }

    @Test
    fun `list shape replaces the whole queue with the page playlist`() {
        val page = MediaQueueDto("playlist", listOf(entry(0), entry(1), entry(2)), cursor = 1, shape = QueueShape.LIST)
        assertEquals(page, QueueResolution.resolve(page, MediaQueueDto.EMPTY))
    }

    @Test
    fun `stream shape publishes what the page offers around the current track`() {
        val page = MediaQueueDto("", listOf(entry(0), entry(1)), cursor = 0, shape = QueueShape.STREAM)
        assertEquals(page, QueueResolution.resolve(page, MediaQueueDto.EMPTY))
    }

    @Test
    fun `the track played just before comes first in a stream, because the page cannot name it`() {
        val page = MediaQueueDto("", listOf(entry(0), entry(1)), cursor = 0, shape = QueueShape.STREAM)
        val resolved = QueueResolution.resolve(page, MediaQueueDto.EMPTY, previousTrack = entry(9))
        assertEquals(listOf(9L, 0L, 1L), resolved.entries.map { it.id })
        assertEquals(1, resolved.cursor)
    }

    @Test
    fun `a stream that already names what comes before is left alone`() {
        val page = MediaQueueDto("", listOf(entry(0), entry(1)), cursor = 1, shape = QueueShape.STREAM)
        assertEquals(page, QueueResolution.resolve(page, MediaQueueDto.EMPTY, previousTrack = entry(9)))
    }

    @Test
    fun `a playlist is never prefixed with what played before it`() {
        val page = MediaQueueDto("playlist", listOf(entry(0), entry(1)), cursor = 0, shape = QueueShape.LIST)
        assertEquals(page, QueueResolution.resolve(page, MediaQueueDto.EMPTY, previousTrack = entry(9)))
    }

    @Test
    fun `the browsing history is the queue only when the page offers none`() {
        val history = MediaQueueDto("history", listOf(entry(0), entry(1)), cursor = 1, shape = QueueShape.NONE)
        assertEquals(history, QueueResolution.resolve(MediaQueueDto.EMPTY, history))
    }

    @Test
    fun `a page queue wins over the browsing history`() {
        val page = MediaQueueDto("playlist", listOf(entry(5)), cursor = 0, shape = QueueShape.LIST)
        val history = MediaQueueDto("history", listOf(entry(0), entry(1)), cursor = 1, shape = QueueShape.NONE)
        assertEquals(page, QueueResolution.resolve(page, history))
    }

    @Test
    fun `none shape with empty history publishes an empty queue`() {
        assertEquals(MediaQueueDto.EMPTY, QueueResolution.resolve(MediaQueueDto.EMPTY, MediaQueueDto.EMPTY))
    }

    @Test
    fun `a declared shape with no entries resolves to an empty queue instead of a cursor out of range`() {
        val page = MediaQueueDto("empty", emptyList(), cursor = 0, shape = QueueShape.LIST)
        assertEquals(MediaQueueDto.EMPTY, QueueResolution.resolve(page, MediaQueueDto.EMPTY))
    }
}
