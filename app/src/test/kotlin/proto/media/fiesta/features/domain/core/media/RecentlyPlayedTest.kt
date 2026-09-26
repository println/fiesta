package proto.media.fiesta.features.domain.core.media

import proto.media.fiesta.support.media.dto.MediaReadingDto
import proto.media.fiesta.support.media.dto.QueueEntryDto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecentlyPlayedTest {

    private val identities = listOf("", "a", "https://example.com/watch|title|author")

    @Test
    fun `a recently played id is always negative, so it never collides with a page or history index`() {
        identities.forEach { identity ->
            assertTrue(identity, RecentlyPlayed.idFor(identity) < 0)
        }
    }

    @Test
    fun `a recently played id is never the unknown queue item id`() {
        val unknownQueueItemId = -1L
        identities.forEach { identity ->
            assertNotEquals(unknownQueueItemId, RecentlyPlayed.idFor(identity))
        }
    }

    @Test
    fun `the same identity always yields the same id`() {
        assertEquals(RecentlyPlayed.idFor("track"), RecentlyPlayed.idFor("track"))
    }

    @Test
    fun `playing a track again moves it to the end instead of duplicating it`() {
        val first = QueueEntryDto(RecentlyPlayed.idFor("a"), "A", "", "")
        val second = QueueEntryDto(RecentlyPlayed.idFor("b"), "B", "", "")

        val entries = RecentlyPlayed.append(RecentlyPlayed.append(listOf(first), second, 20), first, 20)

        assertEquals(listOf(second, first), entries)
    }

    @Test
    fun `a track skipped through is not recorded`() {
        assertFalse(RecentlyPlayed.isWorthRecording(positionSeconds = 3.0, durationSeconds = 240.0))
    }

    @Test
    fun `half a minute in is enough for a long track`() {
        assertTrue(RecentlyPlayed.isWorthRecording(positionSeconds = 30.0, durationSeconds = 600.0))
        assertFalse(RecentlyPlayed.isWorthRecording(positionSeconds = 29.0, durationSeconds = 600.0))
    }

    @Test
    fun `a short track is recorded at a quarter of it, before half a minute`() {
        assertTrue(RecentlyPlayed.isWorthRecording(positionSeconds = 5.0, durationSeconds = 20.0))
        assertFalse(RecentlyPlayed.isWorthRecording(positionSeconds = 4.0, durationSeconds = 20.0))
    }

    @Test
    fun `an unknown duration falls back to half a minute`() {
        assertFalse(RecentlyPlayed.isWorthRecording(positionSeconds = 29.0, durationSeconds = 0.0))
        assertTrue(RecentlyPlayed.isWorthRecording(positionSeconds = 31.0, durationSeconds = 0.0))
    }

    private fun reading(isTrack: Boolean, positionSeconds: Double = 60.0, trackId: String = "track") =
        MediaReadingDto.EMPTY.copy(isTrack = isTrack, positionSeconds = positionSeconds, trackId = trackId)

    @Test
    fun `a plain page is never worth recording`() {
        assertFalse(RecentlyPlayed.shouldRecord(reading(isTrack = false), lastRecordedIdentity = ""))
    }

    @Test
    fun `a track already recorded is not recorded again`() {
        val current = reading(isTrack = true, trackId = "track")
        assertFalse(RecentlyPlayed.shouldRecord(current, lastRecordedIdentity = current.trackIdentity))
    }

    @Test
    fun `a track skipped through is not recorded even if it is a track`() {
        assertFalse(RecentlyPlayed.shouldRecord(reading(isTrack = true, positionSeconds = 3.0), lastRecordedIdentity = ""))
    }

    @Test
    fun `a new track worth recording is recorded`() {
        assertTrue(RecentlyPlayed.shouldRecord(reading(isTrack = true, positionSeconds = 60.0), lastRecordedIdentity = ""))
    }
}
