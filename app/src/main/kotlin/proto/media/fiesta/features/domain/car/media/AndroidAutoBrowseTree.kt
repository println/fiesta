package proto.media.fiesta.features.domain.car.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import proto.media.fiesta.R
import proto.media.fiesta.features.domain.car.player.CarPlayer
import proto.media.fiesta.features.domain.mediaserver.BrowseTree
import proto.media.fiesta.features.domain.mediaserver.PlaybackBrowserService
import proto.media.fiesta.features.domain.mediaserver.PlaybackSession
import proto.media.fiesta.support.media.contract.ClientHandle
import proto.media.fiesta.support.media.contract.MediaClient
import proto.media.fiesta.support.media.dto.MediaEventDto
import proto.media.fiesta.support.media.dto.QueueEntryDto
import proto.media.fiesta.support.media.dto.QueueShape
import proto.media.fiesta.support.media.server.MediaServer

class AndroidAutoBrowseTree(private val context: Context, server: MediaServer) : BrowseTree, MediaClient {

    private lateinit var handle: ClientHandle
    private var lastSeen: List<Any> = emptyList()

    init {
        server.connect(this)
    }

    override fun onConnected(handle: ClientHandle) {
        this.handle = handle
    }

    override fun onEvent(event: MediaEventDto) {
        val state = (event as MediaEventDto.StateChanged).state
        val seen = listOf(state.track.identity, state.queue.entries, state.history)
        if (seen == lastSeen) return
        lastSeen = seen
        TABS.forEach(PlaybackBrowserService::notifyBrowseChanged)
    }

    override fun rootFor(clientPackageName: String): String =
        if (clientPackageName == ANDROID_AUTO_PACKAGE) CAR_ROOT_ID else EMPTY_ROOT_ID

    override fun childrenOf(rootId: String): List<MediaBrowserCompat.MediaItem> = when (rootId) {
        CAR_ROOT_ID -> listOf(
            tab(TAB_HOME, R.string.car_media_tab_home, R.drawable.ic_car_home),
            tab(TAB_PLAYLIST, R.string.car_media_tab_playlist, R.drawable.ic_car_playlist),
            tab(TAB_HISTORY, R.string.car_media_tab_history, R.drawable.ic_car_history)
        )
        TAB_HOME -> listOf(openBrowserItem()) + listOfNotNull(currentTrackItem())
        TAB_PLAYLIST -> pagePlaylist().map(::queueItem)
        TAB_HISTORY -> handle.state.history.map(::queueItem)
        else -> emptyList()
    }

    private fun currentTrackItem(): MediaBrowserCompat.MediaItem? {
        val track = handle.state.track
        if (!track.hasMetadata) return null
        val description = MediaDescriptionCompat.Builder()
            .setMediaId(PlaybackSession.NOW_PLAYING_ID)
            .setTitle(track.title)
            .setSubtitle(track.artist)
            .apply { if (track.artworkUrl.isNotEmpty()) setIconUri(Uri.parse(track.artworkUrl)) }
            .build()
        return MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
    }

    private fun pagePlaylist(): List<QueueEntryDto> {
        val queue = handle.state.queue
        return if (queue.shape == QueueShape.LIST) queue.entries else emptyList()
    }

    override fun onRootBrowsed(rootId: String) {
        if (rootId != CAR_ROOT_ID) return
        PlaybackBrowserService.onCarEntered(context)
        CarPlayer.resumeInterruptedPlayback()
    }

    private fun tab(id: String, titleRes: Int, iconRes: Int): MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
            .setMediaId(id)
            .setTitle(context.getString(titleRes))
            .setIconBitmap(drawableBitmap(iconRes))
            .build()
        return MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE)
    }

    private fun queueItem(entry: QueueEntryDto): MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
            .setMediaId(PlaybackSession.QUEUE_ITEM_PREFIX + entry.id)
            .setTitle(entry.title)
            .setSubtitle(entry.subtitle)
            .apply { if (entry.iconUrl.isNotEmpty()) setIconUri(Uri.parse(entry.iconUrl)) }
            .build()
        return MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
    }

    private fun openBrowserItem(): MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
            .setMediaId(PlaybackSession.OPEN_BROWSER_ID)
            .setTitle(context.getString(R.string.car_media_open_browser))
            .setIconBitmap(drawableBitmap(R.drawable.ic_fiesta_mark))
            .build()
        return MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
    }

    private fun drawableBitmap(resId: Int): Bitmap {
        val drawable = context.getDrawable(resId)!!
        val bitmap = Bitmap.createBitmap(ICON_SIZE_PX, ICON_SIZE_PX, Bitmap.Config.ARGB_8888)
        drawable.setBounds(0, 0, ICON_SIZE_PX, ICON_SIZE_PX)
        drawable.draw(Canvas(bitmap))
        return bitmap
    }

    companion object {
        private const val ICON_SIZE_PX = 192
        private const val EMPTY_ROOT_ID = "root"
        private const val CAR_ROOT_ID = "car_root"
        private const val TAB_HOME = "tab_home"
        private const val TAB_PLAYLIST = "tab_playlist"
        private const val TAB_HISTORY = "tab_history"
        private val TABS = listOf(TAB_HOME, TAB_PLAYLIST, TAB_HISTORY)
        private const val ANDROID_AUTO_PACKAGE = "com.google.android.projection.gearhead"
    }
}
