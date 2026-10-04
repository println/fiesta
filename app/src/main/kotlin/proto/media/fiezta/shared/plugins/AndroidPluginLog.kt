package proto.media.fiezta.shared.plugins

import android.util.Log
import proto.media.fiezta.support.plugins.contract.PluginLog

class AndroidPluginLog(private val tag: String) : PluginLog {

    override fun debug(message: String) {
        Log.d(tag, message)
    }

    override fun warn(message: String, cause: Throwable?) {
        Log.w(tag, message, cause)
    }
}
