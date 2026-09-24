package proto.media.fiesta.features.domain.phone.settings
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.view.View
import android.widget.ListView
import proto.media.fiesta.BuildConfig
import proto.media.fiesta.R
import proto.media.fiesta.features.domain.core.settings.SettingsUtils
import proto.media.fiesta.features.domain.phone.browser.SiteStorageActivity
import proto.media.fiesta.features.domain.phone.plugins.PluginsPhoneActivity
import proto.media.fiesta.support.system.UnlockUtils

class SettingsPhoneFragment : PreferenceFragment() {

    private val realtimeSettingsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        refreshRealtimeSetting(key)
    }

    private fun refreshRealtimeSetting(key: String?) {
        when (key) {
            SettingsUtils.KEY_ZOOM ->
                (findPreference(key) as? ListPreference)?.value = SettingsUtils.getZoomPercent(activity).toString()
            SettingsUtils.KEY_CAR_TOOLBAR_POSITION ->
                (findPreference(key) as? ListPreference)?.value = SettingsUtils.getCarToolbarPosition(activity)
            SettingsUtils.KEY_CAR_TOOLBAR_AUTO_HIDE ->
                (findPreference(key) as? CheckBoxPreference)?.isChecked = SettingsUtils.isCarToolbarAutoHide(activity)
            SettingsUtils.KEY_CAR_BOOKMARKS_BAR ->
                (findPreference(key) as? CheckBoxPreference)?.isChecked = SettingsUtils.isCarBookmarksBarVisible(activity)
        }
    }

    override fun onResume() {
        super.onResume()
        REALTIME_SETTING_KEYS.forEach { refreshRealtimeSetting(it) }
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(realtimeSettingsListener)
    }

    override fun onPause() {
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(realtimeSettingsListener)
        super.onPause()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = view.findViewById<View>(android.R.id.list)
        if (list is ListView) {
            list.divider = null
            list.dividerHeight = 0
        }
    }

    private fun titleWithIcon(title: CharSequence, iconRes: Int): CharSequence {
        val icon = resources.getDrawable(iconRes, activity.theme).mutate()
        val size = (resources.displayMetrics.density * ICON_SIZE_DP).toInt()
        icon.setBounds(0, 0, size, size)
        icon.setTint(resources.getColor(R.color.primaryText, activity.theme))
        return SpannableString("  $title").apply {
            setSpan(ImageSpan(icon, ImageSpan.ALIGN_BOTTOM), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val screenXmlRes = arguments?.getInt(ARG_SCREEN_XML, R.xml.settings) ?: R.xml.settings
        addPreferencesFromResource(screenXmlRes)

        setupHomeUrlPreference()
        setupTopicIndex()
        highlightDonateTopic()

        findPreference("topic_plugins")?.apply {
            title = titleWithIcon(title, R.drawable.mozac_ic_extension_24)
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivity(Intent(activity, PluginsPhoneActivity::class.java))
                true
            }
        }

        findPreference("pref_site_storage")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                startActivity(Intent(activity, SiteStorageActivity::class.java))
                true
            }

        findPreference("pref_unlock")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                UnlockUtils.unlock(activity)
                true
            }

        findPreference("pref_app_version")?.let { appVersion ->
            appVersion.title = getString(R.string.version).trim() + " " + BuildConfig.VERSION_NAME + " · " + BuildConfig.VERSION_CODENAME
        }
    }

    private fun setupHomeUrlPreference() {
        val homeUrl = findPreference("pref_home_url") ?: return
        homeUrl.summary = SettingsUtils.getHomeUrl(activity)
        homeUrl.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            val url = newValue as String?
            val displayUrl = if (!url.isNullOrEmpty()) url else SettingsUtils.DEFAULT_HOME_URL
            preference.summary = displayUrl
            true
        }
    }

    private fun setupTopicIndex() {
        bindTopic("topic_general", R.xml.settings_general, R.string.settings_topic_general_title)
        bindTopic("topic_privacy", R.xml.settings_privacy, R.string.settings_topic_privacy_title)
        bindTopic("topic_car_general", R.xml.settings_car_general, R.string.settings_topic_car_general_title)
        bindTopic("topic_car_youtube", R.xml.settings_car_youtube, R.string.settings_topic_car_youtube_title)
        bindTopic("topic_hack", R.xml.settings_hack, R.string.settings_topic_hack_title)
        bindTopic("topic_donate", R.xml.settings_donate, R.string.settings_topic_donate_title)
        bindTopic("topic_about", R.xml.settings_about, R.string.settings_topic_about_title)
    }

    private fun highlightDonateTopic() {
        val donate = findPreference("topic_donate") ?: return
        val color = resources.getColor(R.color.brandSecondary, activity.theme)
        donate.title = SpannableString(donate.title).apply {
            setSpan(ForegroundColorSpan(color), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun bindTopic(key: String, screenXmlRes: Int, titleRes: Int) {
        val topic = findPreference(key) ?: return
        topic.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(SettingsPhoneActivity.newIntent(activity, screenXmlRes, titleRes))
            true
        }
    }

    companion object {
        private const val ICON_SIZE_DP = 18
        private val REALTIME_SETTING_KEYS = listOf(
            SettingsUtils.KEY_ZOOM,
            SettingsUtils.KEY_CAR_TOOLBAR_POSITION,
            SettingsUtils.KEY_CAR_TOOLBAR_AUTO_HIDE,
            SettingsUtils.KEY_CAR_BOOKMARKS_BAR
        )
        private const val ARG_SCREEN_XML = "arg_screen_xml"

        @JvmStatic
        fun newInstance(screenXmlRes: Int): SettingsPhoneFragment =
            SettingsPhoneFragment().apply {
                arguments = Bundle().apply { putInt(ARG_SCREEN_XML, screenXmlRes) }
            }
    }
}
