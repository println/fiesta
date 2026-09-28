package proto.media.fiesta.support.webviewex.navigation

import android.content.Context

interface SchemeStore {
    fun read(host: String): String?
    fun write(host: String, value: String)
}

class PreferencesSchemeStore(context: Context) : SchemeStore {

    private val preferences = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    override fun read(host: String): String? = preferences.getString(host, null)

    override fun write(host: String, value: String) {
        preferences.edit().putString(host, value).apply()
    }

    private companion object {
        const val FILE = "webviewex_scheme_memory"
    }
}

class SchemeMemory(private val store: SchemeStore) {

    fun remembered(host: String?): Scheme? = when (read(host)) {
        HTTP -> Scheme.HTTP
        HTTPS, HTTPS_PINNED -> Scheme.HTTPS
        else -> null
    }

    fun isPinnedToHttps(host: String?): Boolean = read(host) == HTTPS_PINNED

    fun remember(host: String?, scheme: Scheme) {
        val key = key(host) ?: return
        if (store.read(key) == HTTPS_PINNED) return
        store.write(key, if (scheme == Scheme.HTTP) HTTP else HTTPS)
    }

    fun pinToHttps(host: String?) {
        store.write(key(host) ?: return, HTTPS_PINNED)
    }

    private fun read(host: String?): String? = key(host)?.let(store::read)

    private fun key(host: String?): String? = host?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }

    private companion object {
        const val HTTP = "http"
        const val HTTPS = "https"
        const val HTTPS_PINNED = "https-pinned"
    }
}
