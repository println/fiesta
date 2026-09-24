package proto.media.fiesta.features.domain.mediaserver

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.session.MediaSession
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import proto.media.fiesta.R
import proto.media.fiesta.features.domain.core.media.PlaybackPublication
import proto.media.fiesta.features.domain.core.media.PublishedPlayback
import proto.media.fiesta.support.media.contract.ClientHandle
import proto.media.fiesta.support.media.contract.MediaClient
import proto.media.fiesta.support.media.dto.MediaAction
import proto.media.fiesta.support.media.dto.MediaCommandDto
import proto.media.fiesta.support.media.dto.MediaEventDto
import proto.media.fiesta.support.media.dto.MediaQueueDto
import proto.media.fiesta.support.media.dto.MediaStateDto
import proto.media.fiesta.support.media.dto.MediaStateDto.Playback
import proto.media.fiesta.support.media.dto.QueueEntryDto
import proto.media.fiesta.support.media.dto.RendererAvailability
import proto.media.fiesta.support.media.server.MediaServer
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class PlaybackSession(private val context: Context, server: MediaServer, private val host: Host) : MediaClient {

    interface Host {
        fun openBrowser()
        fun wake(command: MediaCommandDto)
        fun admits(command: MediaCommandDto, caller: String): Boolean
    }

    private lateinit var handle: ClientHandle
    private var seekedDuringPress = false
    private val handler = Handler(Looper.getMainLooper())
    private val artworkExecutor = Executors.newSingleThreadExecutor()
    private var heldSeek: Runnable? = null
    private var artworkLoadedFrom = ""
    private var publishedQueueEntries = emptyList<QueueEntryDto>()
    private var publishedQueueTitle = ""
    private var publishedMetadataKey = ""
    private var publishedArtwork: Bitmap? = null
    private var publishedPlayback: PublishedPlayback? = null

    var artwork: Bitmap? = null
        private set

    private val openBrowserAction = PlaybackStateCompat.CustomAction.Builder(
        OPEN_BROWSER_ID,
        context.getString(R.string.car_media_open_browser),
        R.drawable.ic_fiesta_mark
    ).build()

    private val positionSync = object : Runnable {
        override fun run() {
            val state = handle.state
            publishState(state)
            if (state.playback == Playback.PLAYING) handler.postDelayed(this, POSITION_SYNC_INTERVAL_MS)
        }
    }

    private val session = MediaSessionCompat(context, TAG).apply {
        setCallback(object : MediaSessionCompat.Callback() {
            override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
                val event = mediaButtonEvent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                Log.d(TAG, "Media button: $event")
                if (event == null || !isTrackKey(event.keyCode)) {
                    return super.onMediaButtonEvent(mediaButtonEvent)
                }
                handleTrackKey(event)
                return true
            }

            override fun onPlay() {
                if (admitsFromCaller(MediaCommandDto.Play)) play()
            }

            override fun onPause() {
                if (admitsFromCaller(MediaCommandDto.Pause)) pause()
            }

            override fun onStop() {
                if (admitsFromCaller(MediaCommandDto.Stop)) handle.stop()
            }

            override fun onSkipToNext() = skipToNext()

            override fun onSkipToPrevious() = skipToPrevious()

            override fun onFastForward() = handle.fastForward()

            override fun onRewind() = handle.rewind()

            override fun onSeekTo(pos: Long) {
                handle.seekTo(pos)
                publishState(handle.state)
            }

            override fun onPlayFromSearch(query: String?, extras: Bundle?) {
                Log.d(TAG, "playFromSearch from ${caller()}")
                if (query.isNullOrBlank()) play() else playFromSearch(query)
            }

            override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
                when {
                    mediaId == OPEN_BROWSER_ID -> host.openBrowser()
                    mediaId == NOW_PLAYING_ID -> play()
                    mediaId?.startsWith(QUEUE_ITEM_PREFIX) == true ->
                        handle.skipToQueueItem(mediaId.removePrefix(QUEUE_ITEM_PREFIX).toLong())
                }
            }

            override fun onSkipToQueueItem(id: Long) = skipToQueuePosition(id)

            override fun onCustomAction(action: String?, extras: Bundle?) {
                if (action == OPEN_BROWSER_ID) host.openBrowser()
            }
        })
        isActive = true
    }

    private fun admitsFromCaller(command: MediaCommandDto): Boolean {
        val caller = caller()
        Log.d(TAG, "$command from $caller")
        return host.admits(command, caller)
    }

    private fun caller(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            (session.mediaSession as MediaSession).currentControllerInfo.packageName
        } else {
            "unknown"
        }

    val token: MediaSessionCompat.Token
        get() = session.sessionToken

    init {
        server.connect(this)
    }

    override fun onConnected(handle: ClientHandle) {
        this.handle = handle
    }

    override fun onEvent(event: MediaEventDto) {
        when (event) {
            is MediaEventDto.StateChanged -> publish(event.state)
        }
    }

    fun play() = sendOrWake(MediaCommandDto.Play) { play() }

    fun pause() = handle.pause()

    fun skipToNext() = handle.skipToNext()

    fun skipToPrevious() = handle.skipToPrevious()

    private fun playFromSearch(query: String) = sendOrWake(MediaCommandDto.PlayFromSearch(query)) { playFromSearch(query) }

    private fun sendOrWake(command: MediaCommandDto, viaHandle: ClientHandle.() -> Unit) {
        if (handle.state.renderer.availability == RendererAvailability.ABSENT) {
            host.wake(command)
        } else {
            handle.viaHandle()
        }
    }

    private fun publish(state: MediaStateDto) {
        if (state.track.artworkUrl != artworkLoadedFrom) {
            artwork = null
            loadArtwork(state.track.artworkUrl)
        }
        if (state.playback == Playback.NONE) stopHeldSeek()
        publishMetadata(state)
        publishQueue(state.queue)
        publishState(state)
        keepPositionInSync(state)
    }

    private fun keepPositionInSync(state: MediaStateDto) {
        handler.removeCallbacks(positionSync)
        if (state.playback == Playback.PLAYING) handler.postDelayed(positionSync, POSITION_SYNC_INTERVAL_MS)
    }

    private fun publishQueue(queue: MediaQueueDto) {
        val windowed = queue.window()
        if (windowed.entries == publishedQueueEntries && windowed.title == publishedQueueTitle) return
        publishedQueueEntries = windowed.entries
        publishedQueueTitle = windowed.title
        if (windowed.entries.isEmpty()) {
            session.setQueue(null)
            session.setQueueTitle(null)
            return
        }
        session.setQueue(windowed.entries.mapIndexed(::toQueueItem))
        session.setQueueTitle(windowed.title)
    }

    // MediaSessionCompat identifies a queue item by its position: any other numbering risks
    // QueueItem.UNKNOWN_ID, which the constructor rejects.
    private fun toQueueItem(position: Int, entry: QueueEntryDto): MediaSessionCompat.QueueItem {
        val description = MediaDescriptionCompat.Builder()
            .setMediaId(entry.id.toString())
            .setTitle(entry.title)
            .setSubtitle(entry.subtitle)
            .apply { if (entry.iconUrl.isNotEmpty()) setIconUri(Uri.parse(entry.iconUrl)) }
            .build()
        return MediaSessionCompat.QueueItem(description, position.toLong())
    }

    private fun skipToQueuePosition(position: Long) {
        val entry = publishedQueueEntries.getOrNull(position.toInt()) ?: return
        handle.skipToQueueItem(entry.id)
    }

    private fun activeQueuePositionOf(state: MediaStateDto): Long {
        val cursor = state.queue.window().cursor
        return if (cursor < 0) MediaSessionCompat.QueueItem.UNKNOWN_ID.toLong() else cursor.toLong()
    }

    private fun loadArtwork(url: String) {
        artworkLoadedFrom = url
        if (url.isEmpty()) return
        artworkExecutor.execute {
            val bitmap = downloadBitmap(url)
            handler.post {
                if (artworkLoadedFrom == url && bitmap != null) {
                    artwork = bitmap
                    publishMetadata(handle.state)
                }
            }
        }
    }

    private fun downloadBitmap(url: String): Bitmap? = try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = ARTWORK_TIMEOUT_MS
        connection.readTimeout = ARTWORK_TIMEOUT_MS
        connection.inputStream.use { BitmapFactory.decodeStream(it) }
            .also { connection.disconnect() }
            ?.let(::toCarArtwork)
    } catch (e: Exception) {
        Log.w(TAG, "Artwork download failed: $url", e)
        null
    }

    // Android Auto lays the card out around a square cover, so a wide thumbnail has to be
    // cropped here. Publishing METADATA_KEY_ALBUM_ART_URI undoes this: Android Auto then
    // downloads the original itself and ignores the bitmap.
    private fun toCarArtwork(bitmap: Bitmap): Bitmap {
        val side = minOf(bitmap.width, bitmap.height)
        val square = Bitmap.createBitmap(
            bitmap,
            (bitmap.width - side) / 2,
            (bitmap.height - side) / 2,
            side,
            side
        )
        if (side <= MAX_ARTWORK_SIDE_PX) return square
        return Bitmap.createScaledBitmap(square, MAX_ARTWORK_SIDE_PX, MAX_ARTWORK_SIDE_PX, true)
    }

    private fun publishMetadata(state: MediaStateDto) {
        val track = state.track
        if (track.title.isEmpty()) {
            if (publishedMetadataKey.isEmpty() && publishedArtwork == null) return
            publishedMetadataKey = ""
            publishedArtwork = null
            session.setMetadata(null)
            return
        }
        val durationMillis = state.progress.durationMillis
        val mediaId = browseMediaIdOf(state)
        val key = listOf(
            state.playback.name, track.title, track.artist, track.identity, mediaId,
            durationMillis.toString(), track.artworkUrl
        ).joinToString("\n")
        if (key == publishedMetadataKey && artwork === publishedArtwork) return
        publishedMetadataKey = key
        publishedArtwork = artwork
        val title = if (state.playback == Playback.CONNECTING) {
            context.getString(R.string.car_media_searching_for, track.title)
        } else {
            track.title
        }
        val builder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artist)
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
        if (durationMillis > 0) {
            builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMillis)
        }
        artwork?.let { builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, it) }
        session.setMetadata(builder.build())
    }

    private fun browseMediaIdOf(state: MediaStateDto): String =
        state.queue.current?.let { QUEUE_ITEM_PREFIX + it.id } ?: state.track.identity

    private fun isTrackKey(keyCode: Int) =
        keyCode == KeyEvent.KEYCODE_MEDIA_NEXT || keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS

    private fun handleTrackKey(event: KeyEvent) {
        val forward = event.keyCode == KeyEvent.KEYCODE_MEDIA_NEXT
        when (event.action) {
            KeyEvent.ACTION_DOWN -> if (!isLongPress(event)) {
                stopHeldSeek()
                seekedDuringPress = false
            } else if (!seekedDuringPress) {
                seekedDuringPress = true
                startHeldSeek(forward)
            }
            KeyEvent.ACTION_UP -> {
                stopHeldSeek()
                if (!seekedDuringPress) {
                    if (forward) skipToNext() else skipToPrevious()
                }
                seekedDuringPress = false
            }
        }
    }

    private fun startHeldSeek(forward: Boolean) {
        heldSeek = object : Runnable {
            override fun run() {
                if (forward) handle.fastForward() else handle.rewind()
                handler.postDelayed(this, HELD_SEEK_INTERVAL_MS)
            }
        }.also { it.run() }
    }

    private fun stopHeldSeek() {
        heldSeek?.let { handler.removeCallbacks(it) }
        heldSeek = null
    }

    private fun isLongPress(event: KeyEvent) =
        event.repeatCount > 0 || event.flags and KeyEvent.FLAG_LONG_PRESS != 0

    private fun publishState(state: MediaStateDto) {
        val progress = state.progress
        val next = PublishedPlayback(
            playbackState = playbackStateOf(state.playback),
            actions = actionsOf(state),
            activeQueueId = activeQueuePositionOf(state),
            errorMessage = if (state.playback == Playback.ERROR) state.errorMessage.orEmpty() else "",
            positionMillis = progress.positionMillis,
            speed = progress.speed,
            updatedAtMillis = progress.updatedAtMillis
        )
        if (!PlaybackPublication.worthPublishing(publishedPlayback, next)) return
        publishedPlayback = next
        val builder = PlaybackStateCompat.Builder()
            .setActions(next.actions)
            .addCustomAction(openBrowserAction)
            .setState(next.playbackState, next.positionMillis, next.speed, next.updatedAtMillis)
            .setActiveQueueItemId(next.activeQueueId)
        if (next.errorMessage.isNotEmpty()) {
            builder.setErrorMessage(PlaybackStateCompat.ERROR_CODE_APP_ERROR, next.errorMessage)
        }
        session.setPlaybackState(builder.build())
    }

    private fun actionsOf(state: MediaStateDto): Long {
        var actions = PlaybackStateCompat.ACTION_PLAY_PAUSE or
            PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        state.capabilities.allowed.forEach { actions = actions or playbackActionOf(it) }
        return actions
    }

    private fun playbackActionOf(action: MediaAction): Long = when (action) {
        MediaAction.PLAY -> PlaybackStateCompat.ACTION_PLAY
        MediaAction.PAUSE -> PlaybackStateCompat.ACTION_PAUSE
        MediaAction.STOP -> PlaybackStateCompat.ACTION_STOP
        MediaAction.FAST_FORWARD -> PlaybackStateCompat.ACTION_FAST_FORWARD
        MediaAction.REWIND -> PlaybackStateCompat.ACTION_REWIND
        MediaAction.PLAY_FROM_SEARCH -> PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
        MediaAction.SKIP_TO_NEXT -> PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        MediaAction.SKIP_TO_PREVIOUS -> PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        MediaAction.SEEK_TO -> PlaybackStateCompat.ACTION_SEEK_TO
        MediaAction.SKIP_TO_QUEUE_ITEM -> PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM
    }

    private fun playbackStateOf(playback: Playback): Int = when (playback) {
        Playback.PLAYING -> PlaybackStateCompat.STATE_PLAYING
        Playback.BUFFERING -> PlaybackStateCompat.STATE_BUFFERING
        Playback.CONNECTING -> PlaybackStateCompat.STATE_CONNECTING
        Playback.ERROR -> PlaybackStateCompat.STATE_ERROR
        Playback.STOPPED -> PlaybackStateCompat.STATE_STOPPED
        Playback.PAUSED, Playback.NONE -> PlaybackStateCompat.STATE_PAUSED
    }

    companion object {
        const val OPEN_BROWSER_ID = "open_browser"
        const val NOW_PLAYING_ID = "now_playing"
        const val QUEUE_ITEM_PREFIX = "queue_item:"
        private const val TAG = "PlaybackSession"
        const val HELD_SEEK_INTERVAL_MS = 400L
        const val POSITION_SYNC_INTERVAL_MS = 5_000L
        const val ARTWORK_TIMEOUT_MS = 10_000

        // Android Auto ignores artwork larger than this, and the bitmap crosses Binder on
        // every metadata update.
        private const val MAX_ARTWORK_SIDE_PX = 256
    }
}
