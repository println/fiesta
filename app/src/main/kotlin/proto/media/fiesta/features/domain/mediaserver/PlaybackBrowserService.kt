package proto.media.fiesta.features.domain.mediaserver

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.util.Log
import proto.media.fiesta.config.wiring.MyApplication
import proto.media.fiesta.features.domain.car.player.CarPlayer

class PlaybackBrowserService : MediaBrowserServiceCompat() {

    private var isForeground = false

    override fun onCreate() {
        super.onCreate()
        instance = this
        sessionToken = obtainMediaSession(this).token
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val playback = obtainPlayback(this)
        when (intent?.action) {
            ACTION_FOREGROUND -> refresh(playback.notification, startedAsForeground = true)
            ACTION_PLAY -> playback.session.play()
            ACTION_PAUSE -> playback.session.pause()
            ACTION_NEXT -> playback.session.skipToNext()
            ACTION_PREVIOUS -> playback.session.skipToPrevious()
            ACTION_DISMISS -> CarPlayer.onNotificationDismissed()
        }
        if (isForeground != playback.notification.holdsForeground) refresh(playback.notification)
        return START_NOT_STICKY
    }

    private fun refresh(playbackNotification: PlaybackNotification, startedAsForeground: Boolean = false) {
        val notification = playbackNotification.build()
        val holdsForeground = playbackNotification.holdsForeground
        if (holdsForeground || startedAsForeground) {
            if (!isForeground) Log.d(TAG, "foreground on")
            startForeground(PlaybackNotification.ID, notification)
            isForeground = true
        }
        if (!holdsForeground) {
            if (isForeground) {
                stopForeground(false)
                isForeground = false
                Log.d(TAG, "foreground off, notification stays")
            }
            getSystemService(NotificationManager::class.java)?.notify(PlaybackNotification.ID, notification)
        }
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot {
        Log.d(TAG, "onGetRoot from $clientPackageName")
        return BrowserRoot(browseTree?.rootFor(clientPackageName) ?: EMPTY_ROOT_ID, contentStyle())
    }

    private fun contentStyle() = Bundle().apply {
        putBoolean(CONTENT_STYLE_SUPPORTED, true)
        putInt(CONTENT_STYLE_BROWSABLE_HINT, CONTENT_STYLE_LIST)
        putInt(CONTENT_STYLE_PLAYABLE_HINT, CONTENT_STYLE_LIST)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        Log.d(TAG, "onLoadChildren $parentId")
        result.sendResult(browseTree?.childrenOf(parentId)?.toMutableList() ?: mutableListOf())
        browseTree?.onRootBrowsed(parentId)
    }

    companion object {
        const val ACTION_FOREGROUND = "stream.qorvanel.media.action.FOREGROUND"
        const val ACTION_PLAY = "stream.qorvanel.media.action.PLAY"
        const val ACTION_PAUSE = "stream.qorvanel.media.action.PAUSE"
        const val ACTION_NEXT = "stream.qorvanel.media.action.NEXT"
        const val ACTION_PREVIOUS = "stream.qorvanel.media.action.PREVIOUS"
        const val ACTION_DISMISS = "stream.qorvanel.media.action.DISMISS"
        private const val TAG = "CarPlayer"
        private const val EMPTY_ROOT_ID = "root"
        private const val CONTENT_STYLE_SUPPORTED = "android.media.browse.CONTENT_STYLE_SUPPORTED"
        private const val CONTENT_STYLE_BROWSABLE_HINT = "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT"
        private const val CONTENT_STYLE_PLAYABLE_HINT = "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT"
        private const val CONTENT_STYLE_LIST = 1

        var browseTree: BrowseTree? = null

        fun notifyBrowseChanged(parentId: String) {
            instance?.notifyChildrenChanged(parentId)
        }

        private var instance: PlaybackBrowserService? = null
        private var playback: Playback? = null

        private class Playback(val session: PlaybackSession, val notification: PlaybackNotification)

        @Synchronized
        private fun obtainPlayback(context: Context): Playback = playback ?: createPlayback(context.applicationContext)

        private fun createPlayback(context: Context): Playback {
            val server = MyApplication.mediaServer
            CarPlayer.bind(context, server)
            val session = PlaybackSession(context, server, CarPlayer)
            return Playback(session, PlaybackNotification(context, session, server)).also {
                playback = it
                it.notification.start()
            }
        }

        fun obtainMediaSession(context: Context): PlaybackSession = obtainPlayback(context).session

        fun updatePlaybackNotification(context: Context, force: Boolean = false) {
            val playback = obtainPlayback(context)
            val service = instance
            if (service != null) {
                service.refresh(playback.notification, startedAsForeground = force)
                return
            }
            if (!force && !playback.notification.holdsForeground) return
            val intent = Intent(context, PlaybackBrowserService::class.java).setAction(ACTION_FOREGROUND)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: IllegalStateException) {
                Log.w(TAG, "Could not start the playback service in the foreground", e)
            }
        }

        fun cancelPlaybackNotification(context: Context) {
            instance?.let {
                it.stopForeground(true)
                it.isForeground = false
                it.stopSelf()
            }
            context.getSystemService(NotificationManager::class.java)?.cancel(PlaybackNotification.ID)
        }
    }
}
