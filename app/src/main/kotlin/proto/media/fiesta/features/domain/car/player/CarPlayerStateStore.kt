package proto.media.fiesta.features.domain.car.player

import android.content.Context

class CarPlayerStateStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    val url: String?
        get() = prefs.getString(HOME_URL, null)?.takeIf { it.startsWith("http://") || it.startsWith("https://") }

    fun saveUrl(url: String) {
        prefs.edit().putString(HOME_URL, url).apply()
    }

    fun saveVideoTime(url: String, seconds: Int) {
        prefs.edit().putString(VIDEO_TIME_URL_KEY, url).putInt(VIDEO_TIME_KEY, seconds).apply()
    }

    fun takeVideoTimeFor(url: String?): Int? {
        if (url == null || prefs.getString(VIDEO_TIME_URL_KEY, null) != url) return null
        val seconds = prefs.getInt(VIDEO_TIME_KEY, -1)
        prefs.edit().remove(VIDEO_TIME_URL_KEY).remove(VIDEO_TIME_KEY).apply()
        return seconds.takeIf { it >= 0 }
    }

    var playbackInterrupted: Boolean
        get() = prefs.getBoolean(PLAYBACK_INTERRUPTED_KEY, false)
        set(value) = prefs.edit().putBoolean(PLAYBACK_INTERRUPTED_KEY, value).apply()

    companion object {
        const val PREFS = "car"
        const val HOME_URL = "home_url"
        private const val VIDEO_TIME_KEY = "video_time"
        private const val VIDEO_TIME_URL_KEY = "video_time_url"
        private const val PLAYBACK_INTERRUPTED_KEY = "playback_interrupted"
    }
}
