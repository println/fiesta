package proto.media.fiesta.support.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import proto.media.fiesta.support.media.config.MediaDefaults
import proto.media.fiesta.support.media.dto.MediaCommandDto
import proto.media.fiesta.support.media.dto.MediaStateDto.Playback
import proto.media.fiesta.support.media.dto.RendererAvailability
import proto.media.fiesta.support.media.dto.RendererEventDto
import proto.media.fiesta.support.media.server.MediaServer

class CommandEchoLifecycleTest {

    private val clock = TestClock()
    private val renderer = FakeRenderer()
    private val server = MediaServer(clock::read, clock::scheduleAt)
    private val session = server.register(renderer)
    private val client = RecordingClient()
    private val handle = server.connect(client)

    init {
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
    }

    @Test
    fun `an echo confirmed by the page does not wait for its deadline`() {
        session.report(RendererEventDto.Read(playingVideo(playing = true)))

        handle.pause()

        assertEquals(MediaCommandDto.Pause, session.pendingCommand)

        session.report(RendererEventDto.Read(playingVideo(playing = false)))

        assertNull(session.pendingCommand)
        assertEquals(Playback.PAUSED, client.last.playback)
    }

    @Test
    fun `an echo that expires without confirmation gives the page truth back`() {
        session.report(RendererEventDto.Read(playingVideo(playing = true)))

        handle.pause()

        assertEquals(Playback.PAUSED, client.last.playback)

        clock.advanceTo(MediaDefaults.COMMAND_TIMEOUT_MILLIS + 1)

        assertNull(session.pendingCommand)
        assertEquals(Playback.PLAYING, client.last.playback)
    }

    @Test
    fun `an echo still inside its window survives a contradicting reading`() {
        session.report(RendererEventDto.Read(playingVideo(playing = true)))

        handle.pause()
        clock.advanceTo(MediaDefaults.COMMAND_TIMEOUT_MILLIS - 1)
        session.report(RendererEventDto.Read(playingVideo(playing = true, positionSeconds = 2.0)))

        assertEquals(MediaCommandDto.Pause, session.pendingCommand)
        assertEquals(Playback.PAUSED, client.last.playback)
    }

    @Test
    fun `only play and pause echo, the other commands go straight through`() {
        session.report(RendererEventDto.Read(playingVideo()))

        handle.skipToNext()

        assertNull(session.pendingCommand)
        assertEquals(listOf(MediaCommandDto.SkipToNext), renderer.commands)
    }

    @Test
    fun `expect echoes without dispatching, for a renderer that acted on its own`() {
        session.report(RendererEventDto.Read(playingVideo(playing = true)))

        session.expect(MediaCommandDto.Pause)

        assertEquals(Playback.PAUSED, client.last.playback)
        assertEquals(emptyList<MediaCommandDto>(), renderer.commands)
    }

    @Test
    fun `the optimistic state reaches the client before the renderer is asked`() {
        session.report(RendererEventDto.Read(playingVideo(playing = true)))
        val seen = mutableListOf<String>()
        val watcher = object : proto.media.fiesta.support.media.contract.MediaClient {
            override fun onConnected(handle: proto.media.fiesta.support.media.contract.ClientHandle) = Unit
            override fun onEvent(event: proto.media.fiesta.support.media.dto.MediaEventDto) {
                val state = (event as proto.media.fiesta.support.media.dto.MediaEventDto.StateChanged).state
                seen += "${state.playback} after ${renderer.commands.size} commands"
            }
        }
        server.connect(watcher)

        handle.pause()

        assertEquals(listOf("PLAYING after 0 commands", "PAUSED after 0 commands"), seen)
        assertEquals(listOf(MediaCommandDto.Pause), renderer.commands)
    }
}
