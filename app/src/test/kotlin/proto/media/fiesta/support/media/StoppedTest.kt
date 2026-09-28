package proto.media.fiesta.support.media

import org.junit.Assert.assertEquals
import org.junit.Test
import proto.media.fiesta.support.media.dto.MediaStateDto.Playback
import proto.media.fiesta.support.media.dto.RendererAvailability
import proto.media.fiesta.support.media.dto.RendererEventDto
import proto.media.fiesta.support.media.server.MediaServer

class StoppedTest {

    private val clock = TestClock()
    private val renderer = FakeRenderer()
    private val server = MediaServer(clock::read, clock::scheduleAt)
    private val session = server.register(renderer)
    private val handle = server.connect(RecordingClient())

    init {
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
        session.report(RendererEventDto.Read(playingVideo(title = "Tiny Desk")))
    }

    @Test
    fun `stopping is not the same as pausing`() {
        handle.stop()

        assertEquals(Playback.STOPPED, session.state.playback)
    }

    @Test
    fun `a stop keeps the metadata, so the card still says what it was`() {
        handle.stop()

        assertEquals("Tiny Desk", session.state.track.title)
    }

    @Test
    fun `a page that keeps reporting the same paused track stays stopped`() {
        handle.stop()

        session.report(RendererEventDto.Read(playingVideo(title = "Tiny Desk", playing = false)))

        assertEquals(Playback.STOPPED, session.state.playback)
    }

    @Test
    fun `playing again leaves the stopped state`() {
        handle.stop()

        session.report(RendererEventDto.Read(playingVideo(title = "Tiny Desk", playing = true)))

        assertEquals(Playback.PLAYING, session.state.playback)
    }

    @Test
    fun `another track leaves the stopped state even before it plays`() {
        handle.stop()

        session.report(RendererEventDto.Read(playingVideo(title = "Another one", playing = false)))

        assertEquals(Playback.PAUSED, session.state.playback)
    }

    @Test
    fun `a stopped session that also loses its renderer still says stopped`() {
        handle.stop()

        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.ABSENT))

        assertEquals(Playback.STOPPED, session.state.playback)
    }

    @Test
    fun `an error still wins over a stop`() {
        handle.stop()

        session.report(RendererEventDto.Failed("No connection"))

        assertEquals(Playback.ERROR, session.state.playback)
    }

    @Test
    fun `asking to play right after a stop publishes the optimistic state`() {
        handle.stop()

        handle.play()

        assertEquals(Playback.PLAYING, session.state.playback)
    }
}
