package proto.media.fiesta.features.domain.car.settings

import android.view.View
import android.widget.TextView
import proto.media.fiesta.R

private const val FADE_MILLIS = 200L
private const val VISIBLE_MILLIS = 2000L

fun showCarToast(view: View, text: String) {
    val toast = view.rootView.findViewById<TextView>(R.id.car_toast) ?: return
    toast.animate().cancel()
    toast.text = text
    toast.visibility = View.VISIBLE
    toast.bringToFront()
    toast.animate().alpha(1f).setStartDelay(0).setDuration(FADE_MILLIS).withEndAction {
        toast.animate().alpha(0f).setStartDelay(VISIBLE_MILLIS).setDuration(FADE_MILLIS)
            .withEndAction { toast.visibility = View.GONE }
    }
}
