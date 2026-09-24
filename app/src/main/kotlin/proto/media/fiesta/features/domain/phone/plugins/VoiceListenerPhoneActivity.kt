package proto.media.fiesta.features.domain.phone.plugins

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import proto.media.fiesta.R
import proto.media.fiesta.support.plugins.model.PluginManifest
import proto.media.fiesta.support.plugins.voice.VoiceListener
import proto.media.fiesta.features.domain.core.settings.SettingsUtils
import proto.media.fiesta.shared.plugins.AppPlugins

class VoiceListenerPhoneActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_listener)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val candidates = VoiceListener.candidates(AppPlugins.source(this).stack())
        val options = findViewById<RadioGroup>(R.id.voice_listener_options)
        candidates.forEach { plugin -> options.addView(optionFor(options, plugin)) }
        checkedOptionFor(candidates, options)?.let { options.check(it.id) }
        options.setOnCheckedChangeListener { group, checkedId ->
            SettingsUtils.setVoicePluginId(this, group.findViewById<RadioButton>(checkedId).tag as String)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }

    private fun optionFor(options: RadioGroup, plugin: PluginManifest): RadioButton =
        (layoutInflater.inflate(R.layout.item_voice_listener, options, false) as RadioButton).apply {
            id = View.generateViewId()
            tag = plugin.id
            text = plugin.name
        }

    private fun checkedOptionFor(candidates: List<PluginManifest>, options: RadioGroup): View? {
        val selected = VoiceListener.selected(candidates, SettingsUtils.getVoicePluginId(this)) ?: return null
        return (0 until options.childCount)
            .map { options.getChildAt(it) }
            .firstOrNull { it.tag == selected.id }
    }
}
