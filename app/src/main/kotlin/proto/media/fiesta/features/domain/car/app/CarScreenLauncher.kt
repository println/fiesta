package proto.media.fiesta.features.domain.car.app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.car.Car
import android.support.car.CarConnectionCallback
import android.util.Log
import com.google.android.apps.auto.sdk.service.CarFirstPartyManager

object CarScreenLauncher {

    private const val TAG = "CarScreenLauncher"

    fun open(context: Context) {
        val appContext = context.applicationContext
        try {
            Car.createCar(appContext, object : CarConnectionCallback() {
                override fun onConnected(car: Car) {
                    startCarService(appContext, car)
                    car.disconnect()
                }

                override fun onDisconnected(car: Car) = Unit
            }).connect()
        } catch (e: RuntimeException) {
            Log.w(TAG, "Could not connect to the car", e)
        }
    }

    private fun startCarService(context: Context, car: Car) {
        try {
            val manager = car.getCarManager(CarFirstPartyManager.SERVICE_NAME) as CarFirstPartyManager
            manager.startCarActivity(Intent().setComponent(ComponentName(context, CarService::class.java)))
            Log.i(TAG, "Requested the car screen")
        } catch (e: Exception) {
            Log.w(TAG, "Could not open the car screen", e)
        }
    }
}
