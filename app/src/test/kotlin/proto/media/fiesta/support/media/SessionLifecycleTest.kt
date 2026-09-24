package proto.media.fiesta.support.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import proto.media.fiesta.support.media.dto.MediaStateDto.Playback
import proto.media.fiesta.support.media.dto.QueueEntryDto
import proto.media.fiesta.support.media.dto.RendererAvailability
import proto.media.fiesta.support.media.dto.RendererEventDto
import proto.media.fiesta.support.media.server.MediaServer

class SessionLifecycleTest {

    private val clock = TestClock()
    private val store = InMemoryStore()

    private fun server() = MediaServer(clock::read, clock::scheduleAt, store = store)

    @Test
    fun `what was playing is remembered for the renderer that played it`() {
        val session = server().register(FakeRenderer(id = "car"))
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
        session.report(RendererEventDto.Read(playingVideo(title = "Tiny Desk", positionSeconds = 90.0)))

        val snapshot = store.load("car")

        assertEquals("Tiny Desk", snapshot?.track?.title)
        assertEquals(90_000L, snapshot?.positionMillis)
        assertNull(store.load("phone"))
    }

    @Test
    fun `a cold start restores the last track without claiming it plays`() {
        val first = server().register(FakeRenderer(id = "car"))
        first.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
        first.report(RendererEventDto.Read(playingVideo(title = "Tiny Desk", positionSeconds = 90.0)))

        val restarted = server().register(FakeRenderer(id = "car"))

        assertEquals("Tiny Desk", restarted.state.track.title)
        assertEquals(90_000L, restarted.state.progress.positionMillis)
        assertEquals(Playback.PAUSED, restarted.state.playback)
        assertEquals(RendererAvailability.ABSENT, restarted.state.renderer.availability)
    }

    @Test
    fun `a renderer coming back announces itself and the card follows`() {
        val session = server().register(FakeRenderer(id = "car"))
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
        session.report(RendererEventDto.Read(playingVideo(title = "Tiny Desk")))

        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.ABSENT))
        assertEquals(Playback.PAUSED, session.state.playback)

        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.STARTING))
        assertEquals(Playback.CONNECTING, session.state.playback)

        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
        assertEquals(Playback.PLAYING, session.state.playback)
        assertEquals("Tiny Desk", session.state.track.title)
    }

    @Test
    fun `registering the same renderer again is the same session`() {
        val server = server()
        val renderer = FakeRenderer(id = "car")
        val first = server.register(renderer)

        assertSame(first, server.register(renderer))
    }

    @Test
    fun `another renderer gets a session of its own, inheriting nothing`() {
        val server = server()
        val car = server.register(FakeRenderer(id = "car"))
        car.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
        car.report(RendererEventDto.Read(playingVideo(title = "Tiny Desk")))

        val phone = server.register(FakeRenderer(id = "phone"))

        assertNotSame(car, phone)
        assertEquals("phone", phone.state.renderer.id)
        assertEquals("", phone.state.track.title)
        assertEquals(Playback.NONE, phone.state.playback)
    }

    @Test
    fun `a closed session stops accepting reports`() {
        val server = server()
        val car = server.register(FakeRenderer(id = "car"))
        car.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
        server.register(FakeRenderer(id = "phone"))

        car.report(RendererEventDto.Read(playingVideo(title = "Ghost")))

        assertEquals("", car.state.track.title)
    }

    @Test
    fun `a command with no renderer attached is discarded`() {
        val server = server()
        val renderer = FakeRenderer(id = "car")
        val session = server.register(renderer)
        val handle = server.connect(RecordingClient())

        handle.play()

        assertEquals(RendererAvailability.ABSENT, session.state.renderer.availability)
        assertTrue(renderer.commands.isEmpty())
        assertNull(session.pendingCommand)
    }

    @Test
    fun `a page with no metadata is not worth remembering`() {
        val session = server().register(FakeRenderer(id = "car"))
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))

        assertNull(store.load("car"))
    }

    @Test
    fun `the history a renderer reports reaches every client`() {
        val server = server()
        val session = server.register(FakeRenderer(id = "car"))
        val client = RecordingClient()
        val handle = server.connect(client)
        val played = QueueEntryDto(id = -2, title = "Tiny Desk", subtitle = "NPR", iconUrl = "")

        session.report(RendererEventDto.HistoryRead(listOf(played)))

        assertEquals(listOf(played), handle.state.history)
        assertEquals(listOf(played), session.state.history)
    }
}
