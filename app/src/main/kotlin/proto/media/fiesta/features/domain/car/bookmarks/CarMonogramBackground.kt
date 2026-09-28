package proto.media.fiesta.features.domain.car.bookmarks

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.support.v4.content.ContextCompat
import proto.media.fiesta.R

object CarMonogramBackground {
    private const val FOCUS_RING_WIDTH_DP = 4f
    private const val RIPPLE_COLOR = 0x66FFFFFF
    private const val PRESSED_DARKEN_FACTOR = 0.75f

    fun create(context: Context, fillColor: Int): Drawable {
        val focusRingWidth = (FOCUS_RING_WIDTH_DP * context.resources.displayMetrics.density).toInt()
        val focusRingColor = ContextCompat.getColor(context, R.color.colorSecondary)
        val states = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), circle(darken(fillColor)))
            addState(intArrayOf(android.R.attr.state_focused), circle(fillColor).apply { setStroke(focusRingWidth, focusRingColor) })
            addState(intArrayOf(), circle(fillColor))
        }
        return RippleDrawable(ColorStateList.valueOf(RIPPLE_COLOR), states, circle(Color.WHITE))
    }

    private fun circle(fillColor: Int) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(fillColor)
    }

    private fun darken(color: Int): Int = Color.rgb(
        (Color.red(color) * PRESSED_DARKEN_FACTOR).toInt(),
        (Color.green(color) * PRESSED_DARKEN_FACTOR).toInt(),
        (Color.blue(color) * PRESSED_DARKEN_FACTOR).toInt()
    )
}
