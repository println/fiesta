package proto.media.fiesta.support.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import proto.media.fiesta.support.media.contract.ClientHandle
import proto.media.fiesta.support.media.contract.MediaClient
import proto.media.fiesta.support.media.contract.SessionStore
import proto.media.fiesta.support.media.dto.MediaEventDto
import proto.media.fiesta.support.media.dto.MediaSnapshotDto
import proto.media.fiesta.support.media.dto.RendererAvailability
import proto.media.fiesta.support.media.dto.RendererEventDto
import proto.media.fiesta.support.media.server.MediaServer

class LoggingTest {

    private val clock = TestClock()
    private val log = RecordingLog()

    private fun server(store: SessionStore? = null) =
        MediaServer(clock::read, clock::scheduleAt, store = store, log = log)

    @Test
    fun `registering and replacing a renderer is traced`() {
        val server = server()
        server.register(FakeRenderer(id = "car"))
        server.register(FakeRenderer(id = "phone"))

        assertTrue(log.debugged.any { it == "renderer car registered" })
        assertTrue(log.debugged.any { it == "renderer car replaced by phone, session closed" })
    }

    @Test
    fun `a discarded command says why it was discarded`() {
        val server = server()
        server.register(FakeRenderer(id = "car"))
        server.connect(RecordingClient()).play()

        assertTrue(log.debugged.any { it == "Play discarded, renderer car is absent" })
    }

    @Test
    fun `a dispatched command is traced`() {
        val server = server()
        val session = server.register(FakeRenderer(id = "car"))
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))

        server.connect(RecordingClient()).play()

        assertTrue(log.debugged.any { it == "Play sent to renderer car" })
    }

    @Test
    fun `a renderer failure is an error, not a debug line`() {
        val server = server()
        val session = server.register(FakeRenderer(id = "car"))

        session.report(RendererEventDto.Failed("No connection"))

        assertEquals(listOf("renderer car reported: No connection" to null), log.errors)
    }

    @Test
    fun `a client that throws is reported with its cause, not swallowed`() {
        val server = server()
        val session = server.register(FakeRenderer(id = "car"))
        val boom = IllegalStateException("boom")
        server.connect(object : MediaClient {
            override fun onConnected(handle: ClientHandle) = Unit
            override fun onEvent(event: MediaEventDto) = throw boom
        })

        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))

        assertEquals(
            listOf("client threw while receiving an event and was disconnected" to boom),
            log.errors
        )
    }

    @Test
    fun `a store that fails to load does not stop the session from existing`() {
        val broken = object : SessionStore {
            override fun load(rendererId: String) = throw IllegalStateException("corrupt")
            override fun save(rendererId: String, snapshot: MediaSnapshotDto) = Unit
            override fun clear(rendererId: String) = Unit
        }

        val session = server(broken).register(FakeRenderer(id = "car"))

        assertEquals("car", session.state.renderer.id)
        assertTrue(log.errors.any { it.first == "could not restore the session of renderer car" })
    }

    @Test
    fun `a store that fails to save does not stop playback from being published`() {
        val broken = object : SessionStore {
            override fun load(rendererId: String): MediaSnapshotDto? = null
            override fun save(rendererId: String, snapshot: MediaSnapshotDto) =
                throw IllegalStateException("disk full")

            override fun clear(rendererId: String) = Unit
        }
        val session = server(broken).register(FakeRenderer(id = "car"))
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))

        session.report(RendererEventDto.Read(playingVideo(title = "Tiny Desk")))

        assertEquals("Tiny Desk", session.state.track.title)
        assertTrue(log.errors.any { it.first == "could not remember the session of renderer car" })
    }

    @Test
    fun `a server with no log given stays silent instead of failing`() {
        val session = MediaServer(clock::read, clock::scheduleAt).register(FakeRenderer(id = "car"))

        session.report(RendererEventDto.Failed("No connection"))

        assertTrue(log.errors.isEmpty())
    }
}
