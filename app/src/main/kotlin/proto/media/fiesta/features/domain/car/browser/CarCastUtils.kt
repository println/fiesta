package proto.media.fiesta.features.domain.car.browser

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import proto.media.fiesta.features.domain.car.player.CarPlayer

object CarCastUtils {

    const val ACTION_CAST_URL = "proto.media.fiesta.ACTION_CAST_URL"
    const val ACTION_CAST_PING = "proto.media.fiesta.ACTION_CAST_PING"
    const val KEY_CAST_PENDING_URL = "cast_pending_url"

    fun interface Callback {
        fun onResult(carActive: Boolean)
    }

    @JvmStatic
    fun castUrl(context: Context, url: String?) {
        if (url == null) {
            return
        }
        context.getSharedPreferences(CarPlayer.PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CAST_PENDING_URL, url)
            .commit()
        context.sendBroadcast(Intent(ACTION_CAST_URL).setPackage(context.packageName))
    }

    @JvmStatic
    fun probeCar(context: Context, callback: Callback) {
        context.sendOrderedBroadcast(
            Intent(ACTION_CAST_PING).setPackage(context.packageName), null,
            object : BroadcastReceiver() {
                override fun onReceive(c: Context, i: Intent) {
                    callback.onResult(resultCode == Activity.RESULT_OK)
                }
            }, null, Activity.RESULT_CANCELED, null, null
        )
    }
}
