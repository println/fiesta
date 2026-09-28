package proto.media.fiesta.support.webviewex.gearhead

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import proto.media.fiesta.support.webviewex.error.ErrorPageStyle
import proto.media.fiesta.support.webviewex.media.MediaElementEvent
import proto.media.fiesta.support.webviewex.presentation.RenderMode

interface WebViewExGearhead {
    val allowsExternalApps: Boolean
        get() = true

    val errorPageStyle: ErrorPageStyle?
        get() = null

    fun creationContext(): Context? = null

    fun configure(settings: WebSettings) = Unit

    fun onRenderModeChanged(view: WebView, mode: RenderMode) = Unit

    fun hostWithoutScreen(view: WebView): Context? = null

    fun onAttachedToScreen(view: WebView, container: ViewGroup) = Unit

    fun onViewDestroyed(view: WebView) = Unit

    val playback: PlaybackSnapshot?
        get() = null

    fun observePlayback(listener: (PlaybackSnapshot) -> Unit) = Unit

    fun admits(kind: ObservedCommandKind, origin: String): Boolean = true

    fun onMediaElementEvent(event: MediaElementEvent, positionSeconds: Double, documentId: String): GuardAction =
        GuardAction.None

    fun onDpadCenterOrTap(view: WebView) = Unit

    fun enterKeyboardText(view: WebView, text: String) = Unit

    fun sendKeyboardEnter(view: WebView) = Unit

    fun scrollActiveElementIntoView(view: WebView, alignToTop: Boolean) = Unit

    fun setAspectRatio(view: WebView, mode: String) = Unit

    fun requestFullScreen(view: WebView) = Unit
}
