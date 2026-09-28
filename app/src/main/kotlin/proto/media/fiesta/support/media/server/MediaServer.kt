package proto.media.fiesta.support.media.server

import proto.media.fiesta.support.media.contract.ClientHandle
import proto.media.fiesta.support.media.contract.MediaClient
import proto.media.fiesta.support.media.contract.MediaLog
import proto.media.fiesta.support.media.contract.MediaRenderer
import proto.media.fiesta.support.media.contract.SessionStore
import proto.media.fiesta.support.media.dto.MediaCommandDto
import proto.media.fiesta.support.media.dto.MediaEventDto
import proto.media.fiesta.support.media.dto.MediaStateDto

class MediaServer(
    private val clock: () -> Long,
    private val scheduleAt: (Long, () -> Unit) -> Unit,
    private val policy: ServerPolicy = ServerPolicy.DEFAULT,
    private val store: SessionStore? = null,
    private val log: MediaLog = MediaLog.SILENT
) {
    private val lock = Any()
    private val clients = mutableListOf<MediaClient>()
    private var current: MediaSession? = null

    val session: MediaSession?
        get() = synchronized(lock) { current }

    val state: MediaStateDto
        get() = synchronized(lock) { current?.state ?: MediaStateDto.EMPTY }

    fun register(renderer: MediaRenderer): MediaSession = synchronized(lock) {
        current?.let {
            if (it.rendererId == renderer.id) return it
            log.debug("renderer ${it.rendererId} replaced by ${renderer.id}, session closed")
            it.close()
        }
        log.debug("renderer ${renderer.id} registered")
        MediaSession(lock, renderer, policy, store, clock, scheduleAt, log, ::broadcast).also {
            current = it
            broadcast(it.state)
        }
    }

    fun connect(client: MediaClient): ClientHandle = synchronized(lock) {
        clients += client
        log.debug("client connected, ${clients.size} now listening")
        ConnectedClient(client).also {
            client.onConnected(it)
            deliver(client, MediaEventDto.StateChanged(it.state))
        }
    }

    private fun broadcast(state: MediaStateDto) = synchronized(lock) {
        val event = MediaEventDto.StateChanged(state)
        clients.toList().forEach { deliver(it, event) }
    }

    private fun deliver(client: MediaClient, event: MediaEventDto) {
        try {
            client.onEvent(event)
        } catch (e: Exception) {
            clients -= client
            log.error("client threw while receiving an event and was disconnected", e)
        }
    }

    private inner class ConnectedClient(private val client: MediaClient) : ClientHandle {

        override val state: MediaStateDto
            get() = synchronized(lock) { current?.state ?: MediaStateDto.EMPTY }

        override fun play() = send(MediaCommandDto.Play)

        override fun pause() = send(MediaCommandDto.Pause)

        override fun stop() = send(MediaCommandDto.Stop)

        override fun skipToNext() = send(MediaCommandDto.SkipToNext)

        override fun skipToPrevious() = send(MediaCommandDto.SkipToPrevious)

        override fun fastForward() = send(MediaCommandDto.FastForward)

        override fun rewind() = send(MediaCommandDto.Rewind)

        override fun seekTo(positionMillis: Long) = send(MediaCommandDto.SeekTo(positionMillis))

        override fun playFromSearch(query: String) = send(MediaCommandDto.PlayFromSearch(query))

        override fun skipToQueueItem(id: Long) = send(MediaCommandDto.SkipToQueueItem(id))

        override fun disconnect() = synchronized(lock) {
            clients -= client
            log.debug("client disconnected, ${clients.size} still listening")
        }

        private fun send(command: MediaCommandDto) = synchronized(lock) {
            current?.send(command)
            Unit
        }
    }
}
