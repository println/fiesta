package proto.media.fiesta.support.media

import java.util.concurrent.CountDownLatch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import proto.media.fiesta.support.media.contract.ClientHandle
import proto.media.fiesta.support.media.contract.MediaClient
import proto.media.fiesta.support.media.dto.MediaCommandDto
import proto.media.fiesta.support.media.dto.MediaEventDto
import proto.media.fiesta.support.media.dto.MediaStateDto
import proto.media.fiesta.support.media.dto.RendererAvailability
import proto.media.fiesta.support.media.dto.RendererEventDto
import proto.media.fiesta.support.media.server.MediaServer

class ExclusiveExecutionTest {

    private val clock = TestClock()
    private val renderer = FakeRenderer()
    private val server = MediaServer(clock::read, clock::scheduleAt)
    private val session = server.register(renderer)

    @Test
    fun `concurrent reports never interleave, so every client sees every state exactly once`() {
        val card = RecordingClient()
        val notification = RecordingClient()
        server.connect(card)
        server.connect(notification)
        val seenAtConnect = card.states.size
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))

        val threads = 8
        val perThread = 200
        val start = CountDownLatch(1)
        val workers = (1..threads).map { worker ->
            Thread {
                start.await()
                repeat(perThread) { round ->
                    session.report(RendererEventDto.Read(playingVideo(title = "w$worker-$round")))
                }
            }
        }
        workers.forEach { it.start() }
        start.countDown()
        workers.forEach { it.join() }

        val expected = seenAtConnect + 1 + threads * perThread
        assertEquals(expected, card.states.size)
        assertEquals(expected, notification.states.size)
        assertEquals(card.states, notification.states)
        assertEquals(session.state, card.last)
    }

    @Test
    fun `a client that commands from inside its own callback is served, not deadlocked`() {
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
        val reentrant = object : MediaClient {
            private lateinit var handle: ClientHandle
            var pausedOnce = false

            override fun onConnected(handle: ClientHandle) {
                this.handle = handle
            }

            override fun onEvent(event: MediaEventDto) {
                val state = (event as MediaEventDto.StateChanged).state
                if (state.playback == MediaStateDto.Playback.PLAYING && !pausedOnce) {
                    pausedOnce = true
                    handle.pause()
                }
            }
        }
        server.connect(reentrant)

        session.report(RendererEventDto.Read(playingVideo()))

        assertTrue(reentrant.pausedOnce)
        assertEquals(listOf(MediaCommandDto.Pause), renderer.commands)
        assertEquals(MediaStateDto.Playback.PAUSED, session.state.playback)
    }

    @Test
    fun `a command taken by one caller finishes before the next state is published`() {
        session.report(RendererEventDto.AvailabilityChanged(RendererAvailability.READY))
        session.report(RendererEventDto.Read(playingVideo()))
        val observer = object : MediaClient {
            val playbackWhenRendererWasCalled = mutableListOf<MediaStateDto.Playback>()
            override fun onConnected(handle: ClientHandle) = Unit
            override fun onEvent(event: MediaEventDto) {
                playbackWhenRendererWasCalled += (event as MediaEventDto.StateChanged).state.playback
            }
        }
        val handle = server.connect(observer)

        handle.pause()

        assertEquals(
            listOf(MediaStateDto.Playback.PLAYING, MediaStateDto.Playback.PAUSED),
            observer.playbackWhenRendererWasCalled
        )
        assertEquals(listOf(MediaCommandDto.Pause), renderer.commands)
    }
}
