package proto.media.fiesta.support.media.contract

import proto.media.fiesta.support.media.dto.MediaEventDto
import proto.media.fiesta.support.media.dto.MediaStateDto

interface MediaClient {
    fun onConnected(handle: ClientHandle)
    fun onEvent(event: MediaEventDto)
}

interface ClientHandle {
    val state: MediaStateDto

    fun play()
    fun pause()
    fun stop()
    fun skipToNext()
    fun skipToPrevious()
    fun fastForward()
    fun rewind()
    fun seekTo(positionMillis: Long)
    fun playFromSearch(query: String)
    fun skipToQueueItem(id: Long)
    fun disconnect()
}
