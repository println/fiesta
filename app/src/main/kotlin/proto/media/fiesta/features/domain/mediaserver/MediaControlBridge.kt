package proto.media.fiesta.features.domain.mediaserver

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import proto.media.fiesta.support.media.dto.MediaQueueDto
import proto.media.fiesta.support.media.dto.MediaReadingDto
import proto.media.fiesta.shared.media.MediaQueueJson
import proto.media.fiesta.shared.media.MediaReadingJson

class MediaControlBridge(private val callbacks: Callbacks) {

    private val handler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onReading(json: String) {
        val reading = MediaReadingJson.parse(json) ?: return
        handler.post { callbacks.onReading(reading) }
    }

    @JavascriptInterface
    fun onQueue(json: String) {
        val queue = MediaQueueJson.parse(json)
        handler.post { callbacks.onPageQueue(queue) }
    }

    @JavascriptInterface
    fun onSearchResult(found: Boolean) {
        handler.post { callbacks.onSearchResult(found) }
    }

    interface Callbacks {
        fun onSearchResult(found: Boolean)
        fun onReading(reading: MediaReadingDto)
        fun onPageQueue(queue: MediaQueueDto)
    }
}
