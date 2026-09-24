package proto.media.fiesta.support.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import proto.media.fiesta.support.media.config.MediaDefaults
import proto.media.fiesta.support.media.dto.MediaQueueDto
import proto.media.fiesta.support.media.dto.QueueEntryDto
import proto.media.fiesta.support.media.dto.QueueShape

class MediaQueueDtoTest {

    @Test
    fun `the current entry is the one under the cursor`() {
        assertEquals("b", queueOf("a", "b", "c", cursor = 1).current?.title)
    }

    @Test
    fun `an unknown cursor has no current entry`() {
        assertNull(queueOf("a", "b", cursor = MediaDefaults.UNKNOWN_CURSOR).current)
    }

    @Test
    fun `a cursor past the end has no current entry`() {
        assertNull(queueOf("a", "b", cursor = 7).current)
    }

    @Test
    fun `an empty queue is shapeless and has no cursor`() {
        assertEquals(QueueShape.NONE, MediaQueueDto.EMPTY.shape)
        assertEquals(MediaDefaults.UNKNOWN_CURSOR, MediaQueueDto.EMPTY.cursor)
        assertNull(MediaQueueDto.EMPTY.current)
    }

    @Test
    fun `a window in the middle keeps the same entry under the cursor`() {
        val windowed = queueOf(*titles(0 until 100), cursor = 50).window(radius = 2)

        assertEquals(listOf("48", "49", "50", "51", "52"), windowed.entries.map { it.title })
        assertEquals("50", windowed.current?.title)
    }

    @Test
    fun `a window at the start does not run off the left edge`() {
        val windowed = queueOf(*titles(0 until 100), cursor = 1).window(radius = 5)

        assertEquals(listOf("0", "1", "2", "3", "4", "5", "6"), windowed.entries.map { it.title })
        assertEquals("1", windowed.current?.title)
    }

    @Test
    fun `a window at the end does not run off the right edge`() {
        val windowed = queueOf(*titles(0 until 10), cursor = 9).window(radius = 3)

        assertEquals(listOf("6", "7", "8", "9"), windowed.entries.map { it.title })
        assertEquals("9", windowed.current?.title)
    }

    @Test
    fun `a window with an unknown cursor takes the head of the queue`() {
        val windowed = queueOf(*titles(0 until 100), cursor = MediaDefaults.UNKNOWN_CURSOR).window(radius = 2)

        assertEquals(listOf("0", "1", "2", "3"), windowed.entries.map { it.title })
        assertNull(windowed.current)
    }

    @Test
    fun `the default radius comes from the defaults, not from the call site`() {
        val windowed = queueOf(*titles(0 until 200), cursor = 100).window()

        assertEquals(2 * MediaDefaults.QUEUE_WINDOW_RADIUS + 1, windowed.entries.size)
        assertEquals("100", windowed.current?.title)
    }

    private fun titles(range: IntRange) = range.map { it.toString() }.toTypedArray()

    private fun queueOf(vararg titles: String, cursor: Int) = MediaQueueDto(
        title = "queue",
        entries = titles.mapIndexed { index, title -> QueueEntryDto(index.toLong(), title, "", "") },
        cursor = cursor,
        shape = QueueShape.LIST
    )
}
