package proto.media.fiesta.features.domain.phone.plugins

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import proto.media.fiesta.R
import proto.media.fiesta.support.plugins.model.PluginManifest
import proto.media.fiesta.support.plugins.model.PluginOrigin
import proto.media.fiesta.support.plugins.manifest.PluginPlatforms
import proto.media.fiesta.support.plugins.model.PluginRule
import proto.media.fiesta.support.plugins.model.Requirement
import proto.media.fiesta.support.plugins.model.TweakSpec
import proto.media.fiesta.features.domain.core.settings.SettingsUtils
import proto.media.fiesta.shared.plugins.AppPlugins
import proto.media.fiesta.support.plugins.activation.PluginSource

class PluginDetailsPhoneActivity : AppCompatActivity() {

    private lateinit var source: PluginSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        source = AppPlugins.source(this)
        val pluginId = intent.getStringExtra(EXTRA_PLUGIN_ID)
        val loaded = source.allKnownPlugins().firstOrNull { it.manifest.id == pluginId }
        if (loaded == null) {
            finish()
            return
        }
        val manifest = loaded.manifest

        setContentView(R.layout.activity_plugin_details)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = ""

        findViewById<TextView>(R.id.plugin_details_name).text = manifest.name
        val originText = if (loaded.origin == PluginOrigin.INSTALLED) R.string.plugins_origin_installed else R.string.plugins_origin_default
        findViewById<TextView>(R.id.plugin_details_origin).text = "${getString(originText)} · v${manifest.version}"

        val fallback = source.isFallback(manifest.id)
        findViewById<SwitchCompat>(R.id.plugin_details_active).apply {
            isChecked = source.stack().any { it.id == manifest.id }
            isEnabled = !fallback
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) source.activate(manifest.id) else source.deactivate(manifest.id)
            }
        }

        bindFeatures(manifest)
        findViewById<View>(R.id.plugin_details_remove).apply {
            visibility = if (fallback) View.GONE else View.VISIBLE
            setOnClickListener { confirmRemove(manifest) }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }

    private fun bindFeatures(manifest: PluginManifest) {
        val events = findViewById<LinearLayout>(R.id.plugin_details_events)
        manifest.rules.filter { it.events != null }.forEach { rule ->
            events.addView(featureRow(events, rule.events!!, PluginPlatforms(car = true, phone = false), matchText(rule)))
        }
        if (events.childCount == 0) findViewById<View>(R.id.plugin_details_events_title).visibility = View.GONE

        val tweaks = findViewById<LinearLayout>(R.id.plugin_details_options)
        manifest.rules.forEach { rule ->
            rule.tweaks.forEach { tweak -> tweaks.addView(tweakRow(tweaks, manifest.id, rule, tweak)) }
        }
        if (tweaks.childCount == 0) findViewById<View>(R.id.plugin_details_options_title).visibility = View.GONE
    }

    private fun tweakRow(container: LinearLayout, pluginId: String, rule: PluginRule, tweak: TweakSpec): View {
        val detail = if (tweak.requires == Requirement.ADBLOCK) {
            "${matchText(rule)} · ${getString(R.string.plugins_requires_ad_block)}"
        } else {
            matchText(rule)
        }
        val row = featureRow(container, tweak.script, PluginPlatforms.of(tweak.env), detail)
        val (key, default) = source.effectiveOptionKey(pluginId, tweak.script, tweak.option)
        val available = tweak.requires != Requirement.ADBLOCK || SettingsUtils.isAdBlockEnabled(this)
        val checkBox = row.findViewById<CheckBox>(R.id.plugin_tweak_enabled)
        checkBox.visibility = View.VISIBLE
        checkBox.isEnabled = available
        checkBox.isChecked = available && SettingsUtils.isPluginOptionEnabled(this, key, default)
        row.isEnabled = available
        if (available) {
            row.setOnClickListener {
                checkBox.toggle()
                SettingsUtils.setPluginOptionEnabled(this, key, checkBox.isChecked)
            }
        }
        return row
    }

    private fun featureRow(container: LinearLayout, script: String, platforms: PluginPlatforms, detail: String): View {
        val row = layoutInflater.inflate(R.layout.item_plugin_tweak, container, false)
        row.findViewById<TextView>(R.id.plugin_tweak_name).text = script
        row.findViewById<TextView>(R.id.plugin_tweak_detail).text = detail
        row.bindPlatforms(platforms)
        row.findViewById<View>(R.id.plugin_tweak_enabled).visibility = View.INVISIBLE
        return row
    }

    private fun matchText(rule: PluginRule): String = rule.match.joinToString(", ")

    private fun confirmRemove(manifest: PluginManifest) {
        AlertDialog.Builder(this)
            .setTitle(R.string.plugins_delete)
            .setMessage(getString(R.string.plugins_delete_message, manifest.name))
            .setPositiveButton(R.string.plugins_delete) { _, _ ->
                source.remove(manifest.id)
                finish()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object {
        private const val EXTRA_PLUGIN_ID = "plugin_id"

        fun intent(context: Context, pluginId: String): Intent =
            Intent(context, PluginDetailsPhoneActivity::class.java).putExtra(EXTRA_PLUGIN_ID, pluginId)
    }
}
