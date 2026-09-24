package proto.media.fiesta.features.domain.car.browser

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import android.widget.ImageView
import proto.media.fiesta.R
import proto.media.fiesta.features.domain.core.settings.CarMenuBubblePlacement

class CarMenuBubble(
    private val bubble: ImageView,
    private val onTap: () -> Unit,
    private val onMoved: (xFraction: Float, yFraction: Float) -> Unit
) {

    private val touchSlop = ViewConfiguration.get(bubble.context).scaledTouchSlop
    private val fadeRunnable = Runnable { fade() }
    private var downRawX = 0f
    private var downRawY = 0f
    private var startTranslationX = 0f
    private var startTranslationY = 0f
    private var dragging = false

    init {
        bubble.isFocusable = false
        attachTouchHandling()
    }

    fun show() {
        bubble.visibility = View.VISIBLE
        wakeUp()
    }

    fun showMenuOpen() {
        bubble.setImageResource(R.drawable.ic_menu)
    }

    fun showMenuClosed() {
        bubble.setImageResource(R.drawable.ic_menu_open)
    }

    fun placeAt(xFraction: Float, yFraction: Float, onDoesNotFit: () -> Unit) {
        val parent = bubble.parent as? View ?: return
        if (parent.width > 0 && parent.height > 0) {
            applyPlacement(parent, xFraction, yFraction, onDoesNotFit)
        } else {
            runAfterLayout(parent) { applyPlacement(parent, xFraction, yFraction, onDoesNotFit) }
        }
    }

    private fun applyPlacement(parent: View, xFraction: Float, yFraction: Float, onDoesNotFit: () -> Unit) {
        val params = bubble.layoutParams as FrameLayout.LayoutParams
        val left = CarMenuBubblePlacement.toOffset(xFraction, parent.width)
        val top = CarMenuBubblePlacement.toOffset(yFraction, parent.height)
        if (!CarMenuBubblePlacement.fitsInside(left, top, params.width, params.height, parent.width, parent.height)) {
            onDoesNotFit()
            return
        }
        params.gravity = Gravity.TOP or Gravity.START
        params.setMargins(0, 0, 0, 0)
        bubble.layoutParams = params
        bubble.translationX = left
        bubble.translationY = top
    }

    private fun runAfterLayout(view: View, action: () -> Unit) {
        view.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(v: View, l: Int, t: Int, r: Int, b: Int, ol: Int, ot: Int, or: Int, ob: Int) {
                view.removeOnLayoutChangeListener(this)
                view.post(action)
            }
        })
    }

    fun hide() {
        bubble.removeCallbacks(fadeRunnable)
        bubble.animate().cancel()
        bubble.visibility = View.GONE
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun attachTouchHandling() {
        bubble.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> startGesture(event)
                MotionEvent.ACTION_MOVE -> dragTo(event)
                MotionEvent.ACTION_UP -> finishGesture()
                MotionEvent.ACTION_CANCEL -> scheduleFade()
            }
            true
        }
    }

    private fun startGesture(event: MotionEvent) {
        wakeUp()
        bubble.removeCallbacks(fadeRunnable)
        downRawX = event.rawX
        downRawY = event.rawY
        startTranslationX = bubble.translationX
        startTranslationY = bubble.translationY
        dragging = false
    }

    private fun dragTo(event: MotionEvent) {
        val dx = event.rawX - downRawX
        val dy = event.rawY - downRawY
        if (!dragging && Math.hypot(dx.toDouble(), dy.toDouble()) < touchSlop) return
        dragging = true
        val parent = bubble.parent as? View ?: return
        bubble.translationX = (startTranslationX + dx).coerceIn(-bubble.left.toFloat(), (parent.width - bubble.right).toFloat())
        bubble.translationY = (startTranslationY + dy).coerceIn(-bubble.top.toFloat(), (parent.height - bubble.bottom).toFloat())
    }

    private fun finishGesture() {
        if (dragging) reportPosition() else onTap()
        scheduleFade()
    }

    private fun reportPosition() {
        val parent = bubble.parent as? View ?: return
        onMoved(
            CarMenuBubblePlacement.toFraction(bubble.left + bubble.translationX, parent.width),
            CarMenuBubblePlacement.toFraction(bubble.top + bubble.translationY, parent.height)
        )
    }

    private fun wakeUp() {
        bubble.animate().cancel()
        bubble.alpha = 1f
        setBackgroundKeepingPadding(R.drawable.car_menu_bubble_background)
        scheduleFade()
    }

    private fun setBackgroundKeepingPadding(drawableRes: Int) {
        val left = bubble.paddingLeft
        val top = bubble.paddingTop
        val right = bubble.paddingRight
        val bottom = bubble.paddingBottom
        bubble.setBackgroundResource(drawableRes)
        bubble.setPadding(left, top, right, bottom)
    }

    private fun scheduleFade() {
        bubble.removeCallbacks(fadeRunnable)
        bubble.postDelayed(fadeRunnable, FADE_DELAY_MILLIS)
    }

    private fun fade() {
        bubble.animate().setDuration(FADE_DURATION_MILLIS).alpha(FADED_ALPHA).withEndAction {
            setBackgroundKeepingPadding(R.drawable.car_menu_bubble_faded_background)
        }
    }

    companion object {
        private const val FADE_DELAY_MILLIS = 2000L
        private const val FADE_DURATION_MILLIS = 600L
        private const val FADED_ALPHA = 0.35f
    }
}
