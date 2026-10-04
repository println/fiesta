package proto.media.fiezta.features.domain.car.settings

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import proto.media.fiezta.features.domain.core.settings.SettingsUtils

class CarRealtimeSettingsSync(
    private val context: Context,
    private val onPositionChanged: () -> Unit,
    private val onAutoHideChanged: (Boolean) -> Unit,
    private val onBookmarksBarChanged: (Boolean) -> Unit,
    private val onZoomChanged: (Int) -> Unit
) {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            SettingsUtils.KEY_CAR_TOOLBAR_POSITION,
            SettingsUtils.KEY_CAR_DRIVER_SIDE,
            SettingsUtils.KEY_CAR_STEERING_WHEEL_SIDE ->
                onPositionChanged()
            SettingsUtils.KEY_CAR_TOOLBAR_AUTO_HIDE ->
                onAutoHideChanged(SettingsUtils.isCarToolbarAutoHide(context))
            SettingsUtils.KEY_CAR_BOOKMARKS_BAR ->
                onBookmarksBarChanged(SettingsUtils.isCarBookmarksBarVisible(context))
            SettingsUtils.KEY_ZOOM ->
                onZoomChanged(SettingsUtils.getZoomPercent(context))
        }
    }

    fun register() {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregister() {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
