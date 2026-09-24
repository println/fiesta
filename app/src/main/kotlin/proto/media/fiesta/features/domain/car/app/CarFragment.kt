package proto.media.fiesta.features.domain.car.app

import android.support.annotation.StringRes
import android.support.v4.app.Fragment

open class CarFragment : Fragment() {
    var title: String? = null

    fun setTitle(@StringRes resId: Int) {
        title = context!!.getString(resId)
    }
}
