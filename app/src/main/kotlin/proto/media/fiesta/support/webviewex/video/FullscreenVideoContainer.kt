package proto.media.fiesta.support.webviewex.video

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

// The fullscreen video lives in this container, not in the WebView, so VideoWebView's
// keepPageVisible does not cover it: when the head unit takes the screen for the lane camera,
// the window turns invisible and Chromium suspends the media. Holding the reported window
// visibility is what keeps the audio running with the screen gone.
class FullscreenVideoContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    override fun dispatchWindowVisibilityChanged(visibility: Int) {
        if (childCount > 0) {
            super.dispatchWindowVisibilityChanged(VISIBLE)
        } else {
            super.dispatchWindowVisibilityChanged(visibility)
        }
    }
}
