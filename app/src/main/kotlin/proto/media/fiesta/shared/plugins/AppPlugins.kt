package proto.media.fiesta.shared.plugins

import android.content.Context
import android.os.Handler
import android.os.Looper
import proto.media.fiesta.support.plugins.activation.PluginSource

object AppPlugins {

    @Volatile
    private var source: PluginSource? = null

    fun source(context: Context): PluginSource =
        source ?: synchronized(this) {
            source ?: create(context.applicationContext).also { source = it }
        }

    private fun create(context: Context): PluginSource {
        val log = AndroidPluginLog("PluginSource")
        val mainHandler = Handler(Looper.getMainLooper())
        return PluginSource(
            files = AndroidPluginFiles(context, log),
            store = PreferencesPluginStateStore(context),
            log = log,
            postToMain = { task -> mainHandler.post(task) }
        )
    }
}
