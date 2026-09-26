package proto.media.fiesta.shared.webviewex

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.util.Log
import proto.media.fiesta.support.webviewex.gearhead.CarConnectionSignal

class AndroidAutoConnectionSignal(context: Context) : CarConnectionSignal {

    private val appContext = context.applicationContext

    override fun observe(listener: (connected: Boolean) -> Unit): AutoCloseable {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // Android Auto's provider stops answering once the car is gone, so an update
                // that can no longer be read is a disconnection.
                listener(isConnected() ?: false)
            }
        }
        val filter = IntentFilter(ACTION_CAR_CONNECTION_UPDATED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            appContext.registerReceiver(receiver, filter)
        }
        isConnected()?.let(listener)
        return AutoCloseable { appContext.unregisterReceiver(receiver) }
    }

    private fun isConnected(): Boolean? = try {
        appContext.contentResolver.query(CONNECTION_URI, arrayOf(CONNECTION_STATE_COLUMN), null, null, null)
            ?.use { cursor -> if (cursor.moveToFirst()) cursor.getInt(0) != NOT_CONNECTED else null }
    } catch (e: RuntimeException) {
        Log.w(TAG, "Could not read the car connection", e)
        null
    }

    private companion object {
        const val TAG = "AndroidAutoConnection"
        const val ACTION_CAR_CONNECTION_UPDATED = "androidx.car.app.connection.action.CAR_CONNECTION_UPDATED"
        val CONNECTION_URI: Uri = Uri.parse("content://androidx.car.app.connection")
        const val CONNECTION_STATE_COLUMN = "CarConnectionState"
        const val NOT_CONNECTED = 0
    }
}
