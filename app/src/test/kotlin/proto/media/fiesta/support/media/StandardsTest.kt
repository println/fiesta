package proto.media.fiesta.support.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import proto.media.fiesta.support.media.contract.ClientHandle
import proto.media.fiesta.support.media.contract.MediaClient
import proto.media.fiesta.support.media.dto.MediaEventDto
import proto.media.fiesta.support.media.dto.RendererAvailability
import proto.media.fiesta.support.media.dto.RendererEventDto
import proto.media.fiesta.support.media.server.MediaServer

class StandardsTest {

    private val clock = TestClock()
    private val renderer = FakeRenderer(id = "car")
    private val server = MediaServer(clock::read, clock::scheduleAt)
    private val session = server.register(renderer)

    @Test
    fun `progress carries when it was measured, so a client can extrapolate between reads`() {
        clock.advanceTo(5_000L)
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
        session.report(RendererEventDto.Read(playingVideo(positionSeconds = 30.0)))

        assertEquals(5_000L, session.state.progress.updatedAtMillis)
        assertEquals(30_000L, session.state.progress.positionMillis)

        clock.advanceTo(12_000L)
        session.report(RendererEventDto.Read(playingVideo(positionSeconds = 37.0)))

        assertEquals(12_000L, session.state.progress.updatedAtMillis)
    }

    @Test
    fun `a position that only moved forward is not broadcast, but is readable`() {
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
        session.report(RendererEventDto.Read(playingVideo(positionSeconds = 10.0)))
        val client = RecordingClient()
        server.connect(client)
        val seen = client.states.size

        clock.advanceTo(1_000L)
        session.report(RendererEventDto.Read(playingVideo(positionSeconds = 11.0)))

        assertEquals(seen, client.states.size)
        assertEquals(11_000L, client.handle.state.progress.positionMillis)
    }

    @Test
    fun `the state names which renderer is attached and how available it is`() {
        assertEquals("car", session.state.renderer.id)
        assertEquals(RendererAvailability.ABSENT, session.state.renderer.availability)

        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))

        assertEquals(RendererAvailability.READY, session.state.renderer.availability)
    }

    @Test
    fun `a client that throws is dropped and the others still get the event`() {
        val healthy = RecordingClient()
        server.connect(healthy)
        val broken = object : MediaClient {
            var received = 0
            override fun onConnected(handle: ClientHandle) = Unit
            override fun onEvent(event: MediaEventDto) {
                received++
                throw IllegalStateException("boom")
            }
        }
        server.connect(broken)
        val healthyBefore = healthy.states.size

        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
        session.report(RendererEventDto.Read(playingVideo()))

        assertTrue(healthy.states.size > healthyBefore)
        assertEquals(1, broken.received)
        assertNotEquals(RendererAvailability.ABSENT, healthy.last.renderer.availability)
    }
}
