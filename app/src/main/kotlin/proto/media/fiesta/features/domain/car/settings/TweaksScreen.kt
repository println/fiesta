package proto.media.fiesta.features.domain.car.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import proto.media.fiesta.R
import proto.media.fiesta.features.domain.core.settings.SettingsUtils
import proto.media.fiesta.features.domain.core.tweaks.TweakChanges
import proto.media.fiesta.support.webviewex.WebViewEx

class TweaksScreen(
    private val context: Context,
    private val browser: WebViewEx<*>
) : CarSettingsScreen {

    override val titleRes: Int = R.string.car_settings_tweaks

    private val changes = TweakChanges()
    private lateinit var scrollToPlayerSwitch: Switch

    override fun createView(inflater: LayoutInflater, parent: ViewGroup): View {
        val view = inflater.inflate(R.layout.car_settings_tweaks, parent, false)
        scrollToPlayerSwitch = view.findViewById(R.id.tweak_scroll_to_player_switch)

        val initial = SettingsUtils.isYoutubeScrollToPlayerEnabled(context)
        changes.recordInitial(SettingsUtils.KEY_YOUTUBE_SCROLL_TO_PLAYER, initial)
        scrollToPlayerSwitch.isChecked = initial

        view.findViewById<View>(R.id.tweak_scroll_to_player_row).setOnClickListener {
            val newValue = !scrollToPlayerSwitch.isChecked
            scrollToPlayerSwitch.isChecked = newValue
            SettingsUtils.setYoutubeScrollToPlayerEnabled(context, newValue)
            changes.update(SettingsUtils.KEY_YOUTUBE_SCROLL_TO_PLAYER, newValue)
        }

        return view
    }

    override fun onHidden() {
        if (changes.shouldReload()) {
            browser.reload()
        }
    }
}
