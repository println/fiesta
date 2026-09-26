package proto.media.fiesta.support.media

import proto.media.fiesta.support.media.contract.ClientHandle
import proto.media.fiesta.support.media.contract.MediaClient
import proto.media.fiesta.support.media.contract.MediaLog
import proto.media.fiesta.support.media.contract.MediaRenderer
import proto.media.fiesta.support.media.contract.SessionStore
import proto.media.fiesta.support.media.dto.MediaCommandDto
import proto.media.fiesta.support.media.dto.MediaEventDto
import proto.media.fiesta.support.media.dto.MediaReadingDto
import proto.media.fiesta.support.media.dto.MediaSnapshotDto
import proto.media.fiesta.support.media.dto.MediaStateDto

class FakeRenderer(override val id: String = "car") : MediaRenderer {

    val commands = mutableListOf<MediaCommandDto>()

    override fun execute(command: MediaCommandDto) {
        commands += command
    }
}

class RecordingClient : MediaClient {

    val states = mutableListOf<MediaStateDto>()

    lateinit var handle: ClientHandle
        private set

    val last: MediaStateDto
        get() = states.last()

    override fun onConnected(handle: ClientHandle) {
        this.handle = handle
    }

    override fun onEvent(event: MediaEventDto) {
        when (event) {
            is MediaEventDto.StateChanged -> states += event.state
        }
    }
}

class RecordingLog : MediaLog {

    val debugged = mutableListOf<String>()
    val errors = mutableListOf<Pair<String, Throwable?>>()

    override fun debug(message: String) {
        debugged += message
    }

    override fun error(message: String, cause: Throwable?) {
        errors += message to cause
    }
}

class InMemoryStore : SessionStore {

    private val snapshots = mutableMapOf<String, MediaSnapshotDto>()

    override fun load(rendererId: String): MediaSnapshotDto? = snapshots[rendererId]

    override fun save(rendererId: String, snapshot: MediaSnapshotDto) {
        snapshots[rendererId] = snapshot
    }

    override fun clear(rendererId: String) {
        snapshots -= rendererId
    }
}

class TestClock {

    private val pending = mutableListOf<Pair<Long, () -> Unit>>()

    var now = 0L
        private set

    fun read(): Long = now

    fun scheduleAt(atMillis: Long, action: () -> Unit) {
        pending += atMillis to action
    }

    fun advanceTo(millis: Long) {
        now = millis
        val due = pending.filter { it.first <= millis }
        due.forEach { pending.remove(it) }
        due.forEach { it.second() }
    }
}

fun playingVideo(
    title: String = "Track",
    artist: String = "Artist",
    positionSeconds: Double = 0.0,
    durationSeconds: Double = 212.0,
    playing: Boolean = true,
    canSkipNext: Boolean = true,
    canSkipPrevious: Boolean = true,
    isTrack: Boolean = true
) = MediaReadingDto.EMPTY.copy(
    hasMedia = true,
    playing = playing,
    positionSeconds = positionSeconds,
    durationSeconds = durationSeconds,
    playbackRate = 1.0,
    title = title,
    artist = artist,
    artworkUrl = "https://example.invalid/art.jpg",
    canSkipNext = canSkipNext,
    canSkipPrevious = canSkipPrevious,
    isTrack = isTrack,
    pageUrl = "https://www.youtube.com/watch?v=abc"
)

fun pageWithoutMedia(
    title: String = "A news headline",
    host: String = "news.example",
    pageUrl: String = "https://news.example/story"
) = MediaReadingDto.EMPTY.copy(title = title, artist = host, pageUrl = pageUrl)
