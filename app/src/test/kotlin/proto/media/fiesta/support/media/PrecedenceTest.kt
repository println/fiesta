package proto.media.fiesta.support.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import proto.media.fiesta.support.media.dto.MediaAction
import proto.media.fiesta.support.media.dto.MediaStateDto.Playback
import proto.media.fiesta.support.media.dto.RendererAvailability
import proto.media.fiesta.support.media.dto.RendererEventDto
import proto.media.fiesta.support.media.server.MediaServer

class PrecedenceTest {

    private val clock = TestClock()
    private val renderer = FakeRenderer()
    private val server = MediaServer(clock::read, clock::scheduleAt)
    private val session = server.register(renderer)
    private val client = RecordingClient()

    init {
        server.connect(client)
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
    }

    @Test
    fun `an error wins over a page that says it is playing`() {
        session.report(RendererEventDto.Read(playingVideo()))
        session.report(RendererEventDto.Failed("No connection"))

        assertEquals(Playback.ERROR, client.last.playback)
        assertEquals("No connection", client.last.errorMessage)
    }

    @Test
    fun `an error keeps the last known metadata, so the card is not blank`() {
        session.report(RendererEventDto.Read(playingVideo(title = "Tiny Desk", artist = "NPR")))
        session.report(RendererEventDto.Failed("No connection"))

        assertEquals("Tiny Desk", client.last.track.title)
        assertEquals("NPR", client.last.track.artist)
        assertFalse(MediaAction.SEEK_TO in client.last.capabilities)
    }

    @Test
    fun `clearing the error lets the page truth through again`() {
        session.report(RendererEventDto.Read(playingVideo()))
        session.report(RendererEventDto.Failed("No connection"))
        session.report(RendererEventDto.Failed(null))

        assertEquals(Playback.PLAYING, client.last.playback)
        assertNull(client.last.errorMessage)
    }

    @Test
    fun `preparing wins over a page still playing the previous track`() {
        session.report(RendererEventDto.Read(playingVideo(title = "Old track")))
        session.report(RendererEventDto.Preparing("bossa nova"))

        assertEquals(Playback.CONNECTING, client.last.playback)
        assertEquals("bossa nova", client.last.track.title)
        assertEquals(0L, client.last.progress.positionMillis)
    }

    @Test
    fun `preparing wins over an optimistic command still inside its window`() {
        session.report(RendererEventDto.Read(playingVideo(playing = false)))
        server.connect(RecordingClient()).play()
        session.report(RendererEventDto.Preparing("bossa nova"))

        assertEquals(Playback.CONNECTING, client.last.playback)
        assertEquals("bossa nova", client.last.track.title)
    }

    @Test
    fun `an error wins over preparing`() {
        session.report(RendererEventDto.Preparing("bossa nova"))
        session.report(RendererEventDto.Failed("Nothing found"))

        assertEquals(Playback.ERROR, client.last.playback)
    }

    @Test
    fun `starting connects instead of pretending it plays or that it stopped`() {
        session.report(RendererEventDto.Read(playingVideo(title = "Tiny Desk")))
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.STARTING))

        assertEquals(Playback.CONNECTING, client.last.playback)
        assertEquals("Tiny Desk", client.last.track.title)
        assertFalse(MediaAction.SKIP_TO_NEXT in client.last.capabilities)
    }

    @Test
    fun `starting wins over an optimistic command`() {
        session.report(RendererEventDto.Read(playingVideo(playing = false)))
        server.connect(RecordingClient()).play()
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.STARTING))

        assertEquals(Playback.CONNECTING, client.last.playback)
    }

    @Test
    fun `metadata survives the death of the renderer`() {
        session.report(RendererEventDto.Read(playingVideo(title = "Tiny Desk")))
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.ABSENT))

        assertEquals(Playback.PAUSED, client.last.playback)
        assertEquals("Tiny Desk", client.last.track.title)
        assertFalse(MediaAction.SEEK_TO in client.last.capabilities)
    }

    @Test
    fun `an absent renderer with nothing known is none, not paused`() {
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.ABSENT))

        assertEquals(Playback.NONE, client.last.playback)
    }

    @Test
    fun `an optimistic command wins over an absent renderer`() {
        session.report(RendererEventDto.Read(playingVideo(playing = false)))
        server.connect(RecordingClient()).play()
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.ABSENT))

        assertEquals(Playback.PLAYING, client.last.playback)
    }

    @Test
    fun `a loading page buffers, keeping what it already knew`() {
        session.report(RendererEventDto.Read(playingVideo(title = "Tiny Desk")))
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.LOADING))

        assertEquals(Playback.BUFFERING, client.last.playback)
        assertEquals("Tiny Desk", client.last.track.title)
        assertTrue(MediaAction.SEEK_TO in client.last.capabilities)
        assertEquals(0f, client.last.progress.speed, 0f)
    }

    @Test
    fun `an absent renderer wins over a loading page`() {
        session.report(RendererEventDto.Read(playingVideo(title = "Tiny Desk")))
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.ABSENT))

        assertEquals(Playback.PAUSED, client.last.playback)
    }

    @Test
    fun `with nobody complaining the page decides`() {
        session.report(RendererEventDto.Read(playingVideo(playing = false)))

        assertEquals(Playback.PAUSED, client.last.playback)

        session.report(RendererEventDto.Read(playingVideo(playing = true)))

        assertEquals(Playback.PLAYING, client.last.playback)
    }

    @Test
    fun `seeking needs a known duration, a live renderer and nothing preparing`() {
        session.report(RendererEventDto.Read(playingVideo(durationSeconds = 0.0)))
        assertFalse(MediaAction.SEEK_TO in client.last.capabilities)

        session.report(RendererEventDto.Read(playingVideo(durationSeconds = 212.0)))
        assertTrue(MediaAction.SEEK_TO in client.last.capabilities)

        session.report(RendererEventDto.Preparing("bossa nova"))
        assertFalse(MediaAction.SEEK_TO in client.last.capabilities)
    }

    @Test
    fun `speed is zero whenever nothing is actually playing`() {
        session.report(RendererEventDto.Read(playingVideo(playing = false)))

        assertEquals(0f, client.last.progress.speed, 0f)

        session.report(RendererEventDto.Read(playingVideo(playing = true)))

        assertEquals(1f, client.last.progress.speed, 0f)
    }
}
