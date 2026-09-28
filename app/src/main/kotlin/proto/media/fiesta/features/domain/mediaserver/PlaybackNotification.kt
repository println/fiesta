package proto.media.fiesta.features.domain.mediaserver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.app.NotificationCompat as MediaNotificationCompat
import proto.media.fiesta.R
import proto.media.fiesta.support.media.contract.ClientHandle
import proto.media.fiesta.support.media.contract.MediaClient
import proto.media.fiesta.support.media.dto.MediaAction
import proto.media.fiesta.support.media.dto.MediaEventDto
import proto.media.fiesta.support.media.dto.MediaStateDto.Playback
import proto.media.fiesta.support.media.server.MediaServer

class PlaybackNotification(
    private val context: Context,
    private val session: PlaybackSession,
    private val server: MediaServer
) : MediaClient {

    private lateinit var handle: ClientHandle

    val holdsForeground: Boolean
        get() = handle.state.let { it.audible || it.playback == Playback.CONNECTING }

    fun start() {
        server.connect(this)
    }

    override fun onConnected(handle: ClientHandle) {
        this.handle = handle
    }

    override fun onEvent(event: MediaEventDto) {
        PlaybackBrowserService.updatePlaybackNotification(context)
    }

    fun build(): Notification {
        ensureChannel()
        val state = handle.state
        val playing = state.playback == Playback.PLAYING
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setColor(ContextCompat.getColor(context, R.color.brandPrimary))
            .setColorized(true)
            .setContentTitle(state.track.title.takeIf { it.isNotEmpty() } ?: context.getString(R.string.app_name))
            .setContentText(if (state.playback == Playback.ERROR) state.errorMessage else state.track.artist)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(state.audible)
            .setShowWhen(false)
            .setDeleteIntent(serviceIntent(PlaybackBrowserService.ACTION_DISMISS))

        val compactActionIndices = mutableListOf<Int>()
        if (MediaAction.SKIP_TO_PREVIOUS in state.capabilities) {
            builder.addAction(android.R.drawable.ic_media_previous, "Previous", serviceIntent(PlaybackBrowserService.ACTION_PREVIOUS))
            compactActionIndices += compactActionIndices.size
        }
        builder.addAction(
            if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
            if (playing) "Pause" else "Play",
            serviceIntent(if (playing) PlaybackBrowserService.ACTION_PAUSE else PlaybackBrowserService.ACTION_PLAY)
        )
        compactActionIndices += compactActionIndices.size
        if (MediaAction.SKIP_TO_NEXT in state.capabilities) {
            builder.addAction(android.R.drawable.ic_media_next, "Next", serviceIntent(PlaybackBrowserService.ACTION_NEXT))
            compactActionIndices += compactActionIndices.size
        }
        builder.setStyle(
            MediaNotificationCompat.MediaStyle()
                .setMediaSession(session.token)
                .setShowActionsInCompactView(*compactActionIndices.toIntArray())
        )
        session.artwork?.let { builder.setLargeIcon(it) }
        return builder.build()
    }

    private fun serviceIntent(action: String): PendingIntent {
        val intent = Intent(context, PlaybackBrowserService::class.java).setAction(action)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getService(context, action.hashCode(), intent, flags)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, context.getString(R.string.channel_name), NotificationManager.IMPORTANCE_LOW)
        )
    }

    companion object {
        const val ID = 1
        private const val CHANNEL_ID = "playback"
    }
}
