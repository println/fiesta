package proto.media.fiesta.features.domain.core.settings

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

object SettingsUtils {

    const val KEY_DISABLE_NOTIFICATIONS = "pref_disable_notifications"
    const val KEY_THIRD_PARTY_COOKIES = "pref_third_party_cookies"
    const val KEY_PROTECTED_CONTENT = "pref_protected_content"
    const val KEY_RESUME_ON_FOCUS = "pref_resume_on_focus"
    const val KEY_HOME_URL = "pref_home_url"
    const val KEY_RESTORE_LAST_URL = "pref_restore_last_url"
    const val KEY_STRIP_WV_UA = "pref_strip_wv_user_agent"
    const val KEY_AD_BLOCK = "pref_ad_block"
    const val KEY_SEARCH_SUGGESTIONS = "pref_search_suggestions"
    const val KEY_SAFETY_WARNING = "pref_safety_warning"
    const val KEY_ZOOM = "pref_zoom"
    const val KEY_CAR_TOOLBAR_POSITION = "pref_car_toolbar_position"
    const val KEY_CAR_TOOLBAR_AUTO_HIDE = "pref_car_toolbar_auto_hide"
    const val KEY_CAR_BOOKMARKS_BAR = "pref_car_bookmarks_bar"
    const val KEY_CAR_MENU_BUBBLE_X = "pref_car_menu_bubble_x"
    const val KEY_CAR_MENU_BUBBLE_Y = "pref_car_menu_bubble_y"
    const val KEY_YOUTUBE_SCROLL_TO_PLAYER = "pref_youtube_scroll_to_player"
    const val KEY_DEFAULT_FAVORITES_SEEDED = "pref_default_favorites_seeded"
    const val KEY_VOICE_PLUGIN = "pref_voice_plugin"
    const val KEY_UPDATE_NOTICE = "pref_update_notice"

    const val DEFAULT_HOME_URL = "https://m.youtube.com"

    private fun prefs(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun isDisabledNotifications(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_DISABLE_NOTIFICATIONS, false)
    }

    fun getZoomPercent(context: Context): Int {
        val value = prefs(context).getString(KEY_ZOOM, "100")
        return value?.toIntOrNull() ?: 100
    }

    fun setZoomPercent(context: Context, percent: Int) {
        prefs(context).edit().putString(KEY_ZOOM, percent.toString()).apply()
    }

    fun isSafetyWarningEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_SAFETY_WARNING, false)
    }

    fun isThirdPartyCookiesAllowed(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_THIRD_PARTY_COOKIES, true)
    }

    fun isProtectedContentAllowed(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_PROTECTED_CONTENT, true)
    }

    fun isResumeOnFocus(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_RESUME_ON_FOCUS, true)
    }

    fun getHomeUrl(context: Context): String {
        val url = prefs(context).getString(KEY_HOME_URL, DEFAULT_HOME_URL)
        return if (url.isNullOrEmpty()) DEFAULT_HOME_URL else url
    }

    fun setHomeUrl(context: Context, url: String) {
        prefs(context).edit().putString(KEY_HOME_URL, url).apply()
    }

    fun isRestoreLastUrl(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_RESTORE_LAST_URL, true)
    }

    fun isStripWebViewUserAgent(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_STRIP_WV_UA, false)
    }

    fun isAdBlockEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_AD_BLOCK, true)
    }

    fun setAdBlockEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AD_BLOCK, enabled).apply()
    }

    fun isSearchSuggestionsEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_SEARCH_SUGGESTIONS, true)
    }

    fun isUpdateNoticeEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_UPDATE_NOTICE, true)
    }

    fun disableUpdateNotice(context: Context) {
        prefs(context).edit().putBoolean(KEY_UPDATE_NOTICE, false).apply()
    }



    fun getCarToolbarPosition(context: Context): String {
        return prefs(context).getString(KEY_CAR_TOOLBAR_POSITION, "left")!!
    }

    fun setCarToolbarPosition(context: Context, position: String) {
        prefs(context).edit().putString(KEY_CAR_TOOLBAR_POSITION, position).apply()
    }

    fun isCarToolbarAutoHide(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_CAR_TOOLBAR_AUTO_HIDE, false)
    }

    fun setCarToolbarAutoHide(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_CAR_TOOLBAR_AUTO_HIDE, enabled).apply()
    }

    fun isCarBookmarksBarVisible(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_CAR_BOOKMARKS_BAR, false)
    }

    fun setCarBookmarksBarVisible(context: Context, visible: Boolean) {
        prefs(context).edit().putBoolean(KEY_CAR_BOOKMARKS_BAR, visible).apply()
    }

    fun getCarMenuBubblePosition(context: Context): Pair<Float, Float>? {
        val prefs = prefs(context)
        if (!prefs.contains(KEY_CAR_MENU_BUBBLE_X) || !prefs.contains(KEY_CAR_MENU_BUBBLE_Y)) return null
        return Pair(prefs.getFloat(KEY_CAR_MENU_BUBBLE_X, 0f), prefs.getFloat(KEY_CAR_MENU_BUBBLE_Y, 0f))
    }

    fun setCarMenuBubblePosition(context: Context, xFraction: Float, yFraction: Float) {
        prefs(context).edit().putFloat(KEY_CAR_MENU_BUBBLE_X, xFraction).putFloat(KEY_CAR_MENU_BUBBLE_Y, yFraction).apply()
    }

    fun clearCarMenuBubblePosition(context: Context) {
        prefs(context).edit().remove(KEY_CAR_MENU_BUBBLE_X).remove(KEY_CAR_MENU_BUBBLE_Y).apply()
    }

    fun isYoutubeScrollToPlayerEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_YOUTUBE_SCROLL_TO_PLAYER, true)
    }

    fun getVoicePluginId(context: Context): String? =
        prefs(context).getString(KEY_VOICE_PLUGIN, null)?.takeIf { it.isNotEmpty() }

    fun setVoicePluginId(context: Context, pluginId: String?) {
        prefs(context).edit().putString(KEY_VOICE_PLUGIN, pluginId.orEmpty()).apply()
    }

    fun isPluginOptionEnabled(context: Context, key: String, default: Boolean): Boolean {
        return prefs(context).getBoolean(key, default)
    }

    fun setPluginOptionEnabled(context: Context, key: String, enabled: Boolean) {
        prefs(context).edit().putBoolean(key, enabled).apply()
    }

    fun setYoutubeScrollToPlayerEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_YOUTUBE_SCROLL_TO_PLAYER, enabled).apply()
    }

    fun areDefaultFavoritesSeeded(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_DEFAULT_FAVORITES_SEEDED, false)
    }

    fun markDefaultFavoritesSeeded(context: Context) {
        prefs(context).edit().putBoolean(KEY_DEFAULT_FAVORITES_SEEDED, true).apply()
    }
}
