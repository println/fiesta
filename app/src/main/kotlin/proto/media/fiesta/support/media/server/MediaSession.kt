package proto.media.fiesta.support.media.server

import proto.media.fiesta.support.media.config.MediaDefaults
import proto.media.fiesta.support.media.contract.MediaLog
import proto.media.fiesta.support.media.contract.MediaRenderer
import proto.media.fiesta.support.media.contract.SessionStore
import proto.media.fiesta.support.media.dto.MediaCommandDto
import proto.media.fiesta.support.media.dto.MediaQueueDto
import proto.media.fiesta.support.media.dto.MediaReadingDto
import proto.media.fiesta.support.media.dto.MediaSnapshotDto
import proto.media.fiesta.support.media.dto.MediaStateDto
import proto.media.fiesta.support.media.dto.QueueEntryDto
import proto.media.fiesta.support.media.dto.RendererAvailability
import proto.media.fiesta.support.media.dto.RendererEventDto

class MediaSession internal constructor(
    private val lock: Any,
    private val renderer: MediaRenderer,
    private val policy: ServerPolicy,
    private val store: SessionStore?,
    private val clock: () -> Long,
    private val scheduleAt: (Long, () -> Unit) -> Unit,
    private val log: MediaLog,
    private val onState: (MediaStateDto) -> Unit
) {
    private var reading = MediaReadingDto.EMPTY
    private var queue = MediaQueueDto.EMPTY
    private var history = emptyList<QueueEntryDto>()
    private var availability = RendererAvailability.ABSENT
    private var preparing: String? = null
    private var error: String? = null
    private var echo: CommandEcho? = null
    private var pendingSeekMillis: Long? = null
    private var stopped = false
    private var interrupted = false
    private var open = true

    val rendererId: String
        get() = renderer.id

    private var published = MediaStateDto.EMPTY

    val state: MediaStateDto
        get() = synchronized(lock) { published }

    val pendingCommand: MediaCommandDto?
        get() = synchronized(lock) { echo?.command }

    init {
        synchronized(lock) {
            restore()
            republish()
        }
    }

    fun report(event: RendererEventDto) = synchronized(lock) {
        if (!open) return
        when (event) {
            is RendererEventDto.Read -> {
                if (event.reading.playing || event.reading.trackIdentity != reading.trackIdentity) {
                    stopped = false
                }
                if (event.reading.playing) interrupted = false
                reading = event.reading
                pendingSeekMillis = null
                if (echo?.survives(event.reading, clock()) == false) echo = null
            }
            is RendererEventDto.QueueRead -> queue = event.queue
            is RendererEventDto.HistoryRead -> history = event.entries
            is RendererEventDto.AvailabilityChanged -> {
                if (availability != event.availability) {
                    log.debug("renderer ${renderer.id} is now ${event.availability}")
                }
                availability = event.availability
                if (availability == RendererAvailability.ABSENT) interrupted = false
            }
            is RendererEventDto.Preparing -> {
                event.label?.let { log.debug("renderer ${renderer.id} is preparing $it") }
                preparing = event.label
            }
            is RendererEventDto.Failed -> {
                event.message?.let { log.error("renderer ${renderer.id} reported: $it") }
                error = event.message
            }
            is RendererEventDto.Interrupted -> interrupted = event.interrupted
        }
        republish()
        remember()
    }

    fun send(command: MediaCommandDto) = synchronized(lock) {
        if (!open) return
        if (availability == RendererAvailability.ABSENT) {
            log.debug("$command discarded, renderer ${renderer.id} is absent")
            return
        }
        log.debug("$command sent to renderer ${renderer.id}")
        val stopping = command == MediaCommandDto.Stop
        if (stopped != stopping) {
            stopped = stopping
            republish()
        }
        expect(command)
        if (command is MediaCommandDto.SeekTo) {
            pendingSeekMillis = command.positionMillis
            republish()
        }
        renderer.execute(command)
    }

    fun expect(command: MediaCommandDto) = synchronized(lock) {
        if (!open) return
        val echoed = CommandEcho.of(command, clock() + policy.commandTimeoutMillis) ?: return
        echo = echoed
        republish()
        scheduleAt(echoed.expiresAtMillis) { expire(echoed) }
    }

    fun close() = synchronized(lock) {
        open = false
        log.debug("session of renderer ${renderer.id} closed")
    }

    private fun expire(echoed: CommandEcho) = synchronized(lock) {
        if (echo !== echoed) return
        if (!echoed.survives(reading, clock())) echo = null
        republish()
    }

    private fun republish() {
        val previous = published
        published = project()
        if (worthEventing(previous, published)) onState(published)
    }

    private fun worthEventing(previous: MediaStateDto, next: MediaStateDto): Boolean =
        previous.withoutPosition() != next.withoutPosition()

    private fun MediaStateDto.withoutPosition() =
        copy(progress = progress.copy(positionMillis = 0L, updatedAtMillis = 0L))

    private fun project(): MediaStateDto {
        val projected = StateProjection.project(
            reading = reading,
            queue = queue,
            history = history,
            echo = echo,
            preparing = preparing,
            error = error,
            rendererId = renderer.id,
            availability = availability,
            stopped = stopped,
            interrupted = interrupted,
            nowMillis = clock()
        )
        val seek = pendingSeekMillis ?: return projected
        return projected.copy(progress = projected.progress.copy(positionMillis = seek))
    }

    private fun restore() {
        val snapshot = loadSnapshot() ?: return
        log.debug("restored ${snapshot.track.title} for renderer ${renderer.id}")
        reading = MediaReadingDto.EMPTY.copy(
            title = snapshot.track.title,
            artist = snapshot.track.artist,
            artworkUrl = snapshot.track.artworkUrl,
            trackId = snapshot.track.identity,
            positionSeconds = snapshot.positionMillis / MediaDefaults.MILLIS_PER_SECOND.toDouble(),
            durationSeconds = snapshot.durationMillis / MediaDefaults.MILLIS_PER_SECOND.toDouble()
        )
        queue = snapshot.queue
    }

    private fun loadSnapshot() = try {
        store?.load(renderer.id)
    } catch (e: Exception) {
        log.error("could not restore the session of renderer ${renderer.id}", e)
        null
    }

    private fun remember() {
        val keeper = store ?: return
        if (!published.track.hasMetadata) return
        val snapshot = MediaSnapshotDto(
            track = published.track,
            positionMillis = published.progress.positionMillis,
            durationMillis = published.progress.durationMillis,
            queue = published.queue
        )
        try {
            keeper.save(renderer.id, snapshot)
        } catch (e: Exception) {
            log.error("could not remember the session of renderer ${renderer.id}", e)
        }
    }
}
