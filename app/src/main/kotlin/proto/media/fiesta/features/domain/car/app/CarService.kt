package proto.media.fiesta.features.domain.car.app

import com.google.android.apps.auto.sdk.CarActivity
import com.google.android.apps.auto.sdk.CarActivityService
import proto.media.fiesta.features.domain.mediaserver.PlaybackBrowserService

class CarService : CarActivityService() {

    override fun onCreate() {
        super.onCreate()
        PlaybackBrowserService.obtainMediaSession(this)
    }

    override fun getCarActivity(): Class<out CarActivity> = MainCarActivity::class.java
}
