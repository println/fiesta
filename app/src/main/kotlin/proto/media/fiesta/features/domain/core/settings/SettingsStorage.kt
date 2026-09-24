package proto.media.fiesta.features.domain.core.settings

import android.content.Context
import android.content.SharedPreferences
import okhttp3.HttpUrl

object SettingsStorage {

    private const val PREFS = "site_settings"
    private const val KEY_DESKTOP_PREFIX = "desktop_"
    private const val KEY_AD_BLOCK_PREFIX = "adblock_"

    @JvmStatic
    fun getDesktopOverride(context: Context?, host: String?): Boolean? {
        return get(context, KEY_DESKTOP_PREFIX, host)
    }

    @JvmStatic
    fun setDesktopOverride(context: Context, host: String?, enabled: Boolean) {
        set(context, KEY_DESKTOP_PREFIX, host, enabled)
    }

    @JvmStatic
    fun getAdBlockOverride(context: Context?, host: String?): Boolean? {
        return get(context, KEY_AD_BLOCK_PREFIX, host)
    }

    @JvmStatic
    fun setAdBlockOverride(context: Context, host: String?, enabled: Boolean) {
        set(context, KEY_AD_BLOCK_PREFIX, host, enabled)
    }

    @JvmStatic
    fun isDesktopEnabledForHost(context: Context?, host: String?): Boolean {
        return getDesktopOverride(context, host) ?: false
    }

    @JvmStatic
    fun isAdBlockEnabledForHost(context: Context, host: String?): Boolean {
        return getAdBlockOverride(context, host) ?: SettingsUtils.isAdBlockEnabled(context)
    }

    @JvmStatic
    fun registerChangeListener(context: Context, listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs(context).registerOnSharedPreferenceChangeListener(listener)
    }

    @JvmStatic
    fun unregisterChangeListener(context: Context, listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs(context).unregisterOnSharedPreferenceChangeListener(listener)
    }

    private fun get(context: Context?, prefix: String, host: String?): Boolean? {
        val key = keyFor(prefix, host) ?: return null
        if (context == null) {
            return null
        }
        val prefs = prefs(context)
        return if (prefs.contains(key)) prefs.getBoolean(key, false) else null
    }

    private fun set(context: Context, prefix: String, host: String?, enabled: Boolean) {
        val key = keyFor(prefix, host) ?: return
        prefs(context).edit().putBoolean(key, enabled).apply()
    }

    private fun prefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    internal fun keyFor(prefix: String, host: String?): String? {
        val domain = domainKey(host) ?: return null
        return prefix + domain
    }

    internal fun domainKey(host: String?): String? {
        if (host.isNullOrBlank()) {
            return null
        }
        val lower = host.trim().lowercase()
        return registrableDomain(lower) ?: lower
    }

    private fun registrableDomain(host: String): String? {
        return try {
            HttpUrl.Builder().scheme("https").host(host).build().topPrivateDomain()
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
