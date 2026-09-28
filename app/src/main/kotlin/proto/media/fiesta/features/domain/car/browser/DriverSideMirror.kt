package proto.media.fiesta.features.domain.car.browser

import android.view.View
import android.view.ViewGroup

// The manifest has no android:supportsRtl, so layoutDirection="rtl" is ignored and the
// reversal has to be done by hand. Turning supportsRtl on would mirror the whole app on
// phones set to an RTL language.
object DriverSideMirror {

    const val MARKER = "mirrorWithDriver"

    fun flip(root: View) {
        if (root.tag == MARKER && root is ViewGroup) reverse(root)
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) flip(root.getChildAt(i))
        }
    }

    private fun reverse(group: ViewGroup) {
        val children = (0 until group.childCount).map { group.getChildAt(it) }.reversed()
        group.removeAllViews()
        children.forEach { child ->
            swapHorizontalMargins(child)
            group.addView(child)
        }
    }

    private fun swapHorizontalMargins(child: View) {
        val params = child.layoutParams as? ViewGroup.MarginLayoutParams ?: return
        val start = params.marginStart
        params.marginStart = params.marginEnd
        params.marginEnd = start
        child.layoutParams = params
    }
}
