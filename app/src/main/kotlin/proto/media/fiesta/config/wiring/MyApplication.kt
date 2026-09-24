package proto.media.fiesta.config.wiring

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import io.realm.Realm
import io.realm.RealmConfiguration
import proto.media.fiesta.features.domain.car.media.AndroidAutoBrowseTree
import proto.media.fiesta.features.domain.car.player.CarSessionStore
import proto.media.fiesta.features.domain.mediaserver.PlaybackBrowserService
import proto.media.fiesta.support.media.server.MediaServer
import proto.media.fiesta.shared.media.AndroidMediaLog

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        mediaServer = createMediaServer()
        PlaybackBrowserService.browseTree = AndroidAutoBrowseTree(this, mediaServer)
        Realm.init(this)
        val config = RealmConfiguration.Builder()
            .name("db.realm")
            .schemaVersion(SCHEMA_VERSION)
            .deleteRealmIfMigrationNeeded()
            .allowWritesOnUiThread(true)
            .build()
        Realm.setDefaultConfiguration(config)
        Thread.setDefaultUncaughtExceptionHandler(MyExceptionHandler(this))
    }

    private fun createMediaServer(): MediaServer {
        val handler = Handler(Looper.getMainLooper())
        return MediaServer(
            clock = SystemClock::elapsedRealtime,
            scheduleAt = { atMillis, action -> handler.postDelayed(action, atMillis - SystemClock.elapsedRealtime()) },
            store = CarSessionStore(this),
            log = AndroidMediaLog
        )
    }

    companion object {
        private const val SCHEMA_VERSION = 2L

        lateinit var mediaServer: MediaServer
            private set
    }
}
