package proto.media.fiesta.features.domain.car.app

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.car.Car
import android.support.car.CarConnectionCallback
import android.support.v4.media.session.MediaControllerCompat
import com.google.android.apps.auto.sdk.service.CarFirstPartyManager
import proto.media.fiesta.features.domain.mediaserver.PlaybackBrowserService

class DebugPlayFromSearchReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val component = intent.getStringExtra("component")
        if (component != null) {
            openOnCar(context.applicationContext, ComponentName.unflattenFromString(component)!!)
            return
        }
        val controls = MediaControllerCompat(context, PlaybackBrowserService.obtainMediaSession(context).token).transportControls
        when (intent.getStringExtra("action")) {
            "play" -> controls.play()
            "pause" -> controls.pause()
            "stop" -> controls.stop()
            "next" -> controls.skipToNext()
            else -> controls.playFromSearch(intent.getStringExtra("query") ?: "o rappa", null)
        }
    }

    private fun openOnCar(context: Context, component: ComponentName) {
        Car.createCar(context, object : CarConnectionCallback() {
            override fun onConnected(car: Car) {
                (car.getCarManager(CarFirstPartyManager.SERVICE_NAME) as CarFirstPartyManager)
                    .startCarActivity(Intent().setComponent(component))
                car.disconnect()
            }

            override fun onDisconnected(car: Car) = Unit
        }).connect()
    }
}
