package proto.media.fiesta.features.domain.core.browser

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException

class SearchSuggestClient(
    threadName: String,
    private val debounceMillis: Long,
    private val maxResults: Int = Int.MAX_VALUE
) {

    fun interface Callback {
        fun onResults(suggestions: List<String>)
    }

    private val handlerThread = HandlerThread(threadName)
    private val backgroundHandler: Handler
    private val mainHandler = Handler(Looper.getMainLooper())
    private val client = OkHttpClient()
    private var pending: Runnable? = null

    init {
        handlerThread.start()
        backgroundHandler = Handler(handlerThread.looper)
    }

    fun fetch(url: String?, callback: Callback) {
        pending?.let { backgroundHandler.removeCallbacks(it) }
        if (url.isNullOrBlank()) {
            callback.onResults(emptyList())
            return
        }
        val runnable = Runnable {
            val results = fetchSync(url)
            mainHandler.post { callback.onResults(results) }
        }
        pending = runnable
        backgroundHandler.postDelayed(runnable, debounceMillis)
    }

    private fun fetchSync(url: String): List<String> {
        return try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body
            if (response.isSuccessful && body != null) {
                parseSuggestions(body.string(), maxResults)
            } else {
                emptyList()
            }
        } catch (e: IOException) {
            Log.w(TAG, "search suggest failed", e)
            emptyList()
        } catch (e: JSONException) {
            Log.w(TAG, "search suggest failed", e)
            emptyList()
        }
    }

    fun shutdown() {
        handlerThread.quit()
    }

    companion object {
        private const val TAG = "SearchSuggestClient"

        @JvmStatic
        fun parseSuggestions(json: String, maxResults: Int = Int.MAX_VALUE): List<String> {
            val results = mutableListOf<String>()
            val second = JSONArray(json).opt(1)
            if (second is JSONArray) {
                var i = 0
                while (i < second.length() && results.size < maxResults) {
                    second.opt(i)?.let { results.add(it.toString()) }
                    i++
                }
            }
            return results
        }
    }
}
