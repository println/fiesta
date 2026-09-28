package proto.media.fiesta.features.domain.core.browser
import android.content.Context
import android.content.SharedPreferences
import android.webkit.CookieManager
import android.webkit.ValueCallback
import proto.media.fiesta.support.webviewex.navigation.HostName

object BrowserStorageUtils {

    private const val PREFS = "site_storage"
    private const val KEY_HOSTS = "visited_hosts"

    @JvmStatic
    fun recordHost(context: Context?, url: String?) {
        if (context == null || url == null) {
            return
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return
        }
        val host = HostName.of(url)
        if (host == null || host.isEmpty()) {
            return
        }
        val hosts = getRecordedHosts(context)
        if (hosts.contains(host)) {
            return
        }
        val updated = hosts.toMutableSet()
        updated.add(host)
        prefs(context).edit().putStringSet(KEY_HOSTS, updated).apply()
    }

    @JvmStatic
    fun getRecordedHosts(context: Context?): MutableSet<String> {
        if (context == null) {
            return mutableSetOf()
        }
        val stored = prefs(context).getStringSet(KEY_HOSTS, null)
        return stored?.toMutableSet() ?: mutableSetOf()
    }

    @JvmStatic
    fun forgetHost(context: Context?, host: String?) {
        if (context == null || host == null) {
            return
        }
        val hosts = getRecordedHosts(context)
        if (!hosts.remove(host)) {
            return
        }
        prefs(context).edit().putStringSet(KEY_HOSTS, hosts).apply()
    }

    @JvmStatic
    fun forgetAllHosts(context: Context?) {
        if (context == null) {
            return
        }
        prefs(context).edit().remove(KEY_HOSTS).apply()
    }

    @JvmStatic
    fun countCookies(host: String?): Int {
        if (host == null) {
            return 0
        }
        val cm = CookieManager.getInstance()
        var count = 0
        for (scheme in arrayOf("https://", "http://")) {
            val cookies = cm.getCookie(scheme + host)
            if (cookies == null || cookies.trim().isEmpty()) {
                continue
            }
            count += cookies.split(";").size
        }
        return count
    }

    /**
     * Expires every known cookie for the host and calls `onComplete` once
     * the operation has actually finished. Imperfect by nature: a cookie
     * saved with a Path other than "/" can survive.
     *
     * `CookieManager.setCookie(url, value)` (2 args) is ASYNCHRONOUS - it
     * returns before the write is actually applied. Reloading the list right
     * after would read the old value, because the removal hadn't taken
     * effect yet. That's why this method uses the callback overload (API
     * 21+) and only calls `onComplete` once every write has confirmed.
     */
    @JvmStatic
    fun clearCookiesForHost(host: String?, onComplete: Runnable?) {
        if (host == null) {
            runOnComplete(onComplete)
            return
        }
        val cm = CookieManager.getInstance()
        val toExpire = mutableListOf<Array<String>>()
        for (scheme in arrayOf("https://", "http://")) {
            val url = scheme + host
            val cookies = cm.getCookie(url) ?: continue
            for (pair in cookies.split(";")) {
                val eq = pair.indexOf('=')
                val name = (if (eq > 0) pair.substring(0, eq) else pair).trim()
                if (name.isEmpty()) {
                    continue
                }
                toExpire.add(arrayOf(url, name))
            }
        }

        if (toExpire.isEmpty()) {
            CookieUtils.flush()
            runOnComplete(onComplete)
            return
        }

        var pending = toExpire.size * 2
        val onWritten = ValueCallback<Boolean> {
            pending--
            if (pending <= 0) {
                CookieUtils.flush()
                runOnComplete(onComplete)
            }
        }
        for (entry in toExpire) {
            val url = entry[0]
            val name = entry[1]
            cm.setCookie(url, "$name=; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Path=/", onWritten)
            cm.setCookie(url, "$name=; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Path=/; Domain=.$host", onWritten)
        }
    }

    private fun runOnComplete(onComplete: Runnable?) {
        onComplete?.run()
    }

    private fun prefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }
}
