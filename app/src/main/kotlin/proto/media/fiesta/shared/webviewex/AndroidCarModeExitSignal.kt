package proto.media.fiesta.shared.webviewex

import android.app.UiModeManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import proto.media.fiesta.support.webviewex.gearhead.CarModeExitSignal

class AndroidCarModeExitSignal(context: Context) : CarModeExitSignal {

    private val appContext = context.applicationContext

    override fun observe(listener: () -> Unit): AutoCloseable {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) = listener()
        }
        val filter = IntentFilter(UiModeManager.ACTION_EXIT_CAR_MODE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            appContext.registerReceiver(receiver, filter)
        }
        return AutoCloseable { appContext.unregisterReceiver(receiver) }
    }
}
