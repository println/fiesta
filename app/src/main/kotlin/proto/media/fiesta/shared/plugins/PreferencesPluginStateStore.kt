package proto.media.fiesta.shared.plugins

import android.content.Context
import android.content.SharedPreferences
import proto.media.fiesta.support.plugins.activation.ActivationOrderCodec
import proto.media.fiesta.support.plugins.model.ActivationState
import proto.media.fiesta.support.plugins.contract.PluginStateStore

class PreferencesPluginStateStore(private val context: Context) : PluginStateStore {

    override fun read(): ActivationState {
        val prefs = prefs()
        return ActivationState(
            order = ActivationOrderCodec.decode(prefs.getString(KEY_ACTIVATION_ORDER, null)),
            seenDefaults = ActivationOrderCodec.decode(prefs.getString(KEY_SEEN_DEFAULTS, null)).toSet(),
            removedDefaults = readRemovedDefaults(prefs)
        )
    }

    override fun write(state: ActivationState) {
        prefs().edit()
            .putString(KEY_ACTIVATION_ORDER, ActivationOrderCodec.encode(state.order))
            .putString(KEY_SEEN_DEFAULTS, ActivationOrderCodec.encode(state.seenDefaults.toList()))
            .putStringSet(KEY_REMOVED_DEFAULTS, state.removedDefaults)
            .apply()
    }

    private fun prefs(): SharedPreferences =
        context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)

    private fun readRemovedDefaults(prefs: SharedPreferences): Set<String> =
        when (val stored = prefs.all[KEY_REMOVED_DEFAULTS]) {
            is Set<*> -> stored.filterIsInstance<String>().toSet()
            is String -> ActivationOrderCodec.decode(stored).toSet()
            else -> emptySet()
        }

    private companion object {
        const val KEY_ACTIVATION_ORDER = "pref_plugin_activation_order"
        const val KEY_SEEN_DEFAULTS = "pref_plugin_seen_defaults"
        const val KEY_REMOVED_DEFAULTS = "pref_removed_default_plugins"
    }
}
