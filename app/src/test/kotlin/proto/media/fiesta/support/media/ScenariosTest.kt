package proto.media.fiesta.support.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import proto.media.fiesta.support.media.dto.MediaAction
import proto.media.fiesta.support.media.dto.MediaCommandDto
import proto.media.fiesta.support.media.dto.MediaQueueDto
import proto.media.fiesta.support.media.dto.MediaStateDto
import proto.media.fiesta.support.media.dto.QueueEntryDto
import proto.media.fiesta.support.media.dto.QueueShape
import proto.media.fiesta.support.media.dto.RendererAvailability
import proto.media.fiesta.support.media.dto.RendererEventDto
import proto.media.fiesta.support.media.server.MediaServer

class ScenariosTest {

    private val clock = TestClock()
    private val renderer = FakeRenderer()
    private val server = MediaServer(clock::read, clock::scheduleAt)
    private val session = server.register(renderer)
    private val card = RecordingClient()
    private val notification = RecordingClient()
    private val fromCard = server.connect(card)
    private val fromNotification = server.connect(notification)

    private val clients get() = listOf(card, notification)

    @Test
    fun `youtube video is projected to every connected client`() {
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
        session.report(RendererEventDto.Read(playingVideo(title = "Tiny Desk", artist = "NPR")))

        clients.forEach {
            assertEquals(MediaStateDto.Playback.PLAYING, it.last.playback)
            assertEquals("Tiny Desk", it.last.track.title)
            assertEquals("NPR", it.last.track.artist)
            assertTrue(it.last.audible)
            assertTrue(MediaAction.SEEK_TO in it.last.capabilities)
            assertTrue(MediaAction.SKIP_TO_NEXT in it.last.capabilities)
            assertEquals(212_000L, it.last.progress.durationMillis)
        }
    }

    @Test
    fun `a pause asked by one client is published to both before the page confirms`() {
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
        session.report(RendererEventDto.Read(playingVideo()))

        fromCard.pause()

        assertEquals(listOf(MediaCommandDto.Pause), renderer.commands)
        clients.forEach { assertEquals(MediaStateDto.Playback.PAUSED, it.last.playback) }

        session.report(RendererEventDto.Read(playingVideo(playing = false)))

        clients.forEach { assertEquals(MediaStateDto.Playback.PAUSED, it.last.playback) }
        assertEquals(null, session.pendingCommand)
    }

    @Test
    fun `a seek asked by one client moves the position both clients see`() {
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
        session.report(RendererEventDto.Read(playingVideo(positionSeconds = 4.0)))

        fromNotification.seekTo(60_000L)

        assertEquals(listOf(MediaCommandDto.SeekTo(60_000L)), renderer.commands)
        listOf(fromCard, fromNotification).forEach {
            assertEquals(60_000L, it.state.progress.positionMillis)
        }
    }

    @Test
    fun `the playlist reaches both clients and an item picked on one is dispatched`() {
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
        session.report(RendererEventDto.Read(playingVideo()))
        session.report(RendererEventDto.QueueRead(playlist()))

        clients.forEach {
            assertEquals("Live sessions", it.last.queue.title)
            assertEquals(3, it.last.queue.entries.size)
            assertEquals(1, it.last.queue.cursor)
        }

        clients.forEach { assertTrue(MediaAction.SKIP_TO_QUEUE_ITEM in it.last.capabilities) }

        fromCard.skipToQueueItem(30L)

        assertEquals(MediaCommandDto.SkipToQueueItem(30L), renderer.commands.last())
    }

    @Test
    fun `a page without media is still a track for every client, but not audible`() {
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
        session.report(RendererEventDto.Read(pageWithoutMedia(title = "Eleição", host = "news.example")))

        clients.forEach {
            assertEquals(MediaStateDto.Playback.PLAYING, it.last.playback)
            assertEquals("Eleição", it.last.track.title)
            assertEquals("news.example", it.last.track.artist)
            assertFalse(it.last.audible)
            assertFalse(MediaAction.SEEK_TO in it.last.capabilities)
            assertFalse(MediaAction.SKIP_TO_NEXT in it.last.capabilities)
            assertEquals(0L, it.last.progress.durationMillis)
        }
    }

    @Test
    fun `browsing history is published as the queue to both clients`() {
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
        session.report(RendererEventDto.Read(pageWithoutMedia()))
        session.report(RendererEventDto.QueueRead(history()))

        clients.forEach {
            assertEquals(listOf("Home", "Politics", "Eleição"), it.last.queue.entries.map { entry -> entry.title })
            assertEquals(2, it.last.queue.cursor)
        }

        fromNotification.skipToQueueItem(1L)

        assertEquals(MediaCommandDto.SkipToQueueItem(1L), renderer.commands.last())
    }

    @Test
    fun `both clients see the same sequence of states, and a late client can read the current one`() {
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
        session.report(RendererEventDto.Read(playingVideo()))

        assertEquals(card.states, notification.states)

        val late = RecordingClient()
        val fromLate = server.connect(late)

        assertEquals(1, late.states.size)
        assertEquals(card.last, late.last)
        assertEquals(card.last, fromLate.state)
    }

    @Test
    fun `a disconnected client stops receiving and the others are untouched`() {
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
        val seenBefore = card.states.size

        fromCard.disconnect()
        session.report(RendererEventDto.Read(playingVideo()))

        assertEquals(seenBefore, card.states.size)
        assertEquals(MediaStateDto.Playback.PLAYING, notification.last.playback)
    }

    @Test
    fun `a renderer interruption is connecting, never paused, and ends when the page plays again`() {
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
        session.report(RendererEventDto.Read(playingVideo(positionSeconds = 140.0)))

        session.report(RendererEventDto.Interrupted(true))
        session.report(RendererEventDto.Read(playingVideo(positionSeconds = 0.0, playing = false)))

        assertTrue(card.states.none { it.playback == MediaStateDto.Playback.PAUSED })
        clients.forEach { assertEquals(MediaStateDto.Playback.CONNECTING, it.last.playback) }

        session.report(RendererEventDto.Read(playingVideo(positionSeconds = 140.7)))

        clients.forEach { assertEquals(MediaStateDto.Playback.PLAYING, it.last.playback) }
    }

    @Test
    fun `a pause asked during an interruption still wins`() {
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
        session.report(RendererEventDto.Read(playingVideo()))
        session.report(RendererEventDto.Interrupted(true))

        fromCard.pause()

        clients.forEach { assertEquals(MediaStateDto.Playback.PAUSED, it.last.playback) }
    }

    @Test
    fun `an interruption confirmed as a pause is published as paused`() {
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
        session.report(RendererEventDto.Read(playingVideo()))
        session.report(RendererEventDto.Interrupted(true))
        session.report(RendererEventDto.Read(playingVideo(playing = false)))

        session.report(RendererEventDto.Interrupted(false))

        clients.forEach { assertEquals(MediaStateDto.Playback.PAUSED, it.last.playback) }
    }

    private fun playlist() = MediaQueueDto(
        title = "Live sessions",
        entries = listOf(
            QueueEntryDto(10L, "Opening", "NPR", ""),
            QueueEntryDto(20L, "Tiny Desk", "NPR", ""),
            QueueEntryDto(30L, "Encore", "NPR", "")
        ),
        cursor = 1,
        shape = QueueShape.LIST
    )

    private fun history() = MediaQueueDto(
        title = "news.example",
        entries = listOf(
            QueueEntryDto(0L, "Home", "news.example", ""),
            QueueEntryDto(1L, "Politics", "news.example", ""),
            QueueEntryDto(2L, "Eleição", "news.example", "")
        ),
        cursor = 2,
        shape = QueueShape.NONE
    )
}
