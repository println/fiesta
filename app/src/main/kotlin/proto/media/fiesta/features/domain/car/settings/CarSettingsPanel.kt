package proto.media.fiesta.features.domain.car.settings

import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.Switch
import proto.media.fiesta.R
import proto.media.fiesta.features.domain.core.bookmarks.BookmarkUtils
import proto.media.fiesta.features.domain.core.settings.SettingsStorage
import proto.media.fiesta.features.domain.core.settings.SettingsUtils
import proto.media.fiesta.features.domain.core.settings.ZoomStep
import proto.media.fiesta.support.webviewex.WebViewEx
import io.realm.Realm

class CarSettingsPanel(
    private val panelView: View,
    private val browser: WebViewEx<*>,
    private val currentHost: () -> String?,
    private val callbacks: Callbacks
) {

    interface Callbacks {
        fun isFullScreen(): Boolean
        fun toggleFullScreen()
    }

    private val context get() = panelView.context

    private val favoriteButton: Button = panelView.findViewById(R.id.car_settings_favorite_button)
    private val fullscreenButton: Button = panelView.findViewById(R.id.car_settings_fullscreen_button)
    private val zoomOutButton: Button = panelView.findViewById(R.id.car_settings_zoom_out_button)
    private val zoomInButton: Button = panelView.findViewById(R.id.car_settings_zoom_in_button)
    private val zoomResetButton: Button = panelView.findViewById(R.id.car_settings_zoom_reset_button)
    private val desktopSwitch: Switch = panelView.findViewById(R.id.car_settings_desktop_switch)
    private val adBlockSwitch: Switch = panelView.findViewById(R.id.car_settings_adblock_switch)
    private val toolbarPositionLeft: RadioButton = panelView.findViewById(R.id.car_settings_toolbar_position_left)
    private val toolbarPositionRight: RadioButton = panelView.findViewById(R.id.car_settings_toolbar_position_right)
    private val toolbarPositionTop: RadioButton = panelView.findViewById(R.id.car_settings_toolbar_position_top)
    private val toolbarPositionBottom: RadioButton = panelView.findViewById(R.id.car_settings_toolbar_position_bottom)
    private val toolbarAutoHideSwitch: Switch = panelView.findViewById(R.id.car_settings_toolbar_auto_hide_switch)
    private val bookmarksBarSwitch: Switch = panelView.findViewById(R.id.car_settings_bookmarks_bar_switch)

    private val siteSettingsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        if (panelView.visibility == View.VISIBLE) {
            refreshHostSwitches()
        }
    }

    private val realtimeSettingsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (panelView.visibility != View.VISIBLE) return@OnSharedPreferenceChangeListener
        when (key) {
            SettingsUtils.KEY_CAR_TOOLBAR_POSITION -> refreshToolbarPositionRadios()
            SettingsUtils.KEY_CAR_TOOLBAR_AUTO_HIDE -> refreshToolbarAutoHideSwitch()
            SettingsUtils.KEY_CAR_BOOKMARKS_BAR -> refreshBookmarksBarSwitch()
            SettingsUtils.KEY_ZOOM -> refreshZoomLabel()
        }
    }

    init {
        favoriteButton.setOnClickListener { toggleFavorite() }
        fullscreenButton.setOnClickListener { callbacks.toggleFullScreen() }
        zoomOutButton.setOnClickListener { step(-1) }
        zoomInButton.setOnClickListener { step(+1) }
        zoomResetButton.setOnClickListener { resetZoom() }
        desktopSwitch.setOnClickListener { toggleDesktopSite() }
        adBlockSwitch.setOnClickListener { toggleAdBlock() }
        toolbarPositionLeft.setOnClickListener { setToolbarPosition("left") }
        toolbarPositionRight.setOnClickListener { setToolbarPosition("right") }
        toolbarPositionTop.setOnClickListener { setToolbarPosition("top") }
        toolbarPositionBottom.setOnClickListener { setToolbarPosition("bottom") }
        toolbarAutoHideSwitch.setOnClickListener {
            SettingsUtils.setCarToolbarAutoHide(context, toolbarAutoHideSwitch.isChecked)
        }
        bookmarksBarSwitch.setOnClickListener {
            SettingsUtils.setCarBookmarksBarVisible(context, bookmarksBarSwitch.isChecked)
        }
        SettingsStorage.registerChangeListener(context, siteSettingsListener)
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(realtimeSettingsListener)
    }

    fun release() {
        SettingsStorage.unregisterChangeListener(context, siteSettingsListener)
        PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(realtimeSettingsListener)
    }

    fun refresh() {
        refreshFavoriteIcon()
        refreshFullScreenButton()
        refreshZoomLabel()
        refreshHostSwitches()
        refreshToolbarPositionRadios()
        refreshToolbarAutoHideSwitch()
        refreshBookmarksBarSwitch()
    }

    private fun toggleFavorite() {
        val url = browser.url
        val realm = Realm.getDefaultInstance()
        val existing = BookmarkUtils.findByUrl(realm, url)
        val preventDelete = existing?.isPreventDelete == true
        val isBookmarked = existing != null
        realm.close()

        when {
            preventDelete -> showCarToast(panelView, context.getString(R.string.car_settings_bookmark_locked))
            isBookmarked -> {
                BookmarkUtils.removeByUrl(url)
                showCarToast(panelView, context.getString(R.string.car_settings_bookmark_removed))
                refreshFavoriteIcon()
            }
            else -> {
                BookmarkUtils.addBookmark(browser) { refreshFavoriteIcon() }
                showCarToast(panelView, context.getString(R.string.car_settings_bookmark_added))
            }
        }
    }

    private fun refreshFavoriteIcon() {
        val realm = Realm.getDefaultInstance()
        val isBookmarked = BookmarkUtils.findByUrl(realm, browser.url) != null
        realm.close()
        val icon = if (isBookmarked) R.drawable.ic_car_favorite_filled else R.drawable.ic_car_favorite
        favoriteButton.setCompoundDrawablesWithIntrinsicBounds(0, icon, 0, 0)
        favoriteButton.isActivated = isBookmarked
    }

    private fun refreshFullScreenButton() {
        fullscreenButton.isActivated = callbacks.isFullScreen()
    }

    private fun step(direction: Int) {
        applyZoom(ZoomStep.next(SettingsUtils.getZoomPercent(context), direction))
    }

    private fun resetZoom() {
        applyZoom(ZoomStep.DEFAULT)
    }

    private fun applyZoom(target: Int) {
        SettingsUtils.setZoomPercent(context, target)
        refreshZoomLabel()
    }

    private fun refreshZoomLabel() {
        val zoom = SettingsUtils.getZoomPercent(context)
        zoomResetButton.text = context.getString(R.string.car_settings_zoom_label, zoom)
        zoomOutButton.isActivated = zoom < ZoomStep.DEFAULT
        zoomResetButton.isActivated = zoom == ZoomStep.DEFAULT
        zoomInButton.isActivated = zoom > ZoomStep.DEFAULT
    }

    private fun toggleDesktopSite() {
        val host = currentHost() ?: return
        val enabled = desktopSwitch.isChecked
        SettingsStorage.setDesktopOverride(context, host, enabled)
        val message = if (enabled) R.string.car_settings_desktop_on else R.string.car_settings_desktop_off
        showCarToast(panelView, context.getString(message))
        browser.reload()
    }

    private fun toggleAdBlock() {
        val host = currentHost() ?: return
        val enabled = adBlockSwitch.isChecked
        SettingsStorage.setAdBlockOverride(context, host, enabled)
        val message = if (enabled) R.string.car_settings_adblock_on else R.string.car_settings_adblock_off
        showCarToast(panelView, context.getString(message))
        browser.reload()
    }

    private fun refreshHostSwitches() {
        val host = currentHost()
        desktopSwitch.isEnabled = host != null
        adBlockSwitch.isEnabled = host != null
        desktopSwitch.isChecked = host != null && SettingsStorage.isDesktopEnabledForHost(context, host)
        adBlockSwitch.isChecked = host != null && SettingsStorage.isAdBlockEnabledForHost(context, host)
    }

    private fun setToolbarPosition(position: String) {
        SettingsUtils.setCarToolbarPosition(context, position)
    }

    private fun refreshToolbarPositionRadios() {
        val position = SettingsUtils.getCarToolbarPosition(context)
        toolbarPositionLeft.isChecked = position == "left"
        toolbarPositionRight.isChecked = position == "right"
        toolbarPositionTop.isChecked = position == "top"
        toolbarPositionBottom.isChecked = position == "bottom"
    }

    private fun refreshToolbarAutoHideSwitch() {
        toolbarAutoHideSwitch.isChecked = SettingsUtils.isCarToolbarAutoHide(context)
    }

    private fun refreshBookmarksBarSwitch() {
        bookmarksBarSwitch.isChecked = SettingsUtils.isCarBookmarksBarVisible(context)
    }
}
