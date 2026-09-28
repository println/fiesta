package proto.media.fiesta.support.webviewex.gearhead

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import proto.media.fiesta.support.webviewex.WebViewExConfig
import proto.media.fiesta.support.webviewex.error.ErrorPageStyle
import proto.media.fiesta.support.webviewex.media.MediaElementEvent
import proto.media.fiesta.support.webviewex.presentation.RenderMode

class AndroidAutoWebViewExGearhead(
    context: Context,
    audioOutput: AudioOutputSignal,
    carConnection: CarConnectionSignal,
    carModeExit: CarModeExitSignal,
    private val backgroundPolicy: BackgroundAudioPolicy = BackgroundAudioPolicy()
) : WebViewExGearhead {

    private val appContext = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private var state = AutoHostState.NO_VIEW
    private var backgroundWindow: BackgroundWindow? = null
    private var screenSpec = DisplaySpec.CAR_DEFAULT
    private val shrinkBackgroundWindow = Runnable {
        backgroundWindow?.resize(screenSpec.shrunkTo(backgroundPolicy.shrunkWidth, backgroundPolicy.shrunkHeight))
    }

    private val transitionGuard = ScreenTransitionGuard(SystemClock::elapsedRealtime, ANDROID_AUTO_PACKAGE)
    private val verdictObserver = PlaybackVerdictObserver(
        clockMillis = SystemClock::elapsedRealtime,
        scheduleAfter = { delay, action -> handler.postDelayed(action, delay) },
        onVerdictChanged = { snapshot, cause ->
            Log.d(TAG, "$snapshot after $cause")
            playbackListener?.invoke(snapshot)
        }
    )

    var onCarModeExited: (() -> Unit)? = null
    private var playbackListener: ((PlaybackSnapshot) -> Unit)? = null

    override val playback: PlaybackSnapshot
        get() = verdictObserver.snapshot

    private var carConnected = false

    init {
        carModeExit.observe(::onCarModeExit)
        audioOutput.observe(verdictObserver::onAudioOutput)
        carConnection.observe(::onCarConnectionChanged)
    }

    override val allowsExternalApps: Boolean
        get() = false

    override val errorPageStyle: ErrorPageStyle
        get() = ErrorPageStyle.COMPACT

    override fun creationContext(): Context = backgroundWindow().windowContext

    override fun configure(settings: WebSettings) {
        settings.setSupportZoom(false)
        settings.builtInZoomControls = false
        settings.loadWithOverviewMode = true
    }

    override fun onRenderModeChanged(view: WebView, mode: RenderMode) {
        (view as? KeepsPageVisible)?.keepPageVisible = true
        handler.removeCallbacks(shrinkBackgroundWindow)
        if (mode == RenderMode.BACKGROUND) {
            handler.postDelayed(shrinkBackgroundWindow, backgroundPolicy.shrinkAfterMillis)
        }
    }

    override fun hostWithoutScreen(view: WebView): Context? = perform(AutoHostEvent.LEFT_WITHOUT_SCREEN, view)

    override fun onAttachedToScreen(view: WebView, container: ViewGroup) {
        val cameFromBackgroundWindow = state == AutoHostState.IN_BACKGROUND_WINDOW
        val metrics = container.resources.displayMetrics
        screenSpec = DisplaySpec(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi)
        backgroundWindow().resize(screenSpec)
        perform(AutoHostEvent.ATTACHED_TO_SCREEN, view)
        if (cameFromBackgroundWindow) {
            val current = playback
            transitionGuard.armIfPlaying(current.expectsPlayback, current.trustedPositionSeconds)
        }
    }

    override fun onViewDestroyed(view: WebView) {
        handler.removeCallbacks(shrinkBackgroundWindow)
        perform(AutoHostEvent.VIEW_DESTROYED, view)
        verdictObserver.onViewDestroyed()
    }

    override fun observePlayback(listener: (PlaybackSnapshot) -> Unit) {
        playbackListener = listener
    }

    override fun admits(kind: ObservedCommandKind, origin: String): Boolean {
        val command = ObservedCommand(kind, origin, SystemClock.elapsedRealtime())
        if (!transitionGuard.admits(command)) {
            Log.d(TAG, "$kind from $origin ignored, it is the automatic pause of the screen attach")
            return false
        }
        verdictObserver.onCommand(command)
        return true
    }

    override fun onMediaElementEvent(event: MediaElementEvent, positionSeconds: Double, documentId: String): GuardAction {
        if (event != MediaElementEvent.TIME_UPDATE) Log.d(TAG, "element $event at $positionSeconds")
        verdictObserver.onMediaElementEvent(event, positionSeconds, documentId)
        val action = transitionGuard.onEvent(event, positionSeconds, playback.trustedPositionSeconds)
        if (action != GuardAction.None) Log.d(TAG, "$event at $positionSeconds -> $action")
        return action
    }

    override fun onDpadCenterOrTap(view: WebView) {
        view.showCarKeyboardIfInput()
    }

    override fun enterKeyboardText(view: WebView, text: String) {
        view.enterCarKeyboardText(text)
    }

    override fun sendKeyboardEnter(view: WebView) {
        view.sendCarKeyboardEnter()
    }

    override fun scrollActiveElementIntoView(view: WebView, alignToTop: Boolean) {
        view.scrollCarActiveElementIntoView(alignToTop)
    }

    override fun setAspectRatio(view: WebView, mode: String) {
        view.setCarAspectRatio(mode)
    }

    override fun requestFullScreen(view: WebView) {
        view.requestCarFullScreen()
    }

    private fun perform(event: AutoHostEvent, view: WebView): Context? {
        val transition = AutoHostMachine.next(state, event)
        Log.d(TAG, "$state --$event--> ${transition.state} (${transition.effect})")
        state = transition.state
        return when (transition.effect) {
            AutoHostEffect.HOST_IN_BACKGROUND_WINDOW -> hostInBackgroundWindow(view)
            AutoHostEffect.RELEASE_BACKGROUND_WINDOW -> {
                releaseBackgroundWindow()
                null
            }
            AutoHostEffect.NONE -> null
        }
    }

    private fun hostInBackgroundWindow(view: WebView): Context {
        val window = backgroundWindow()
        window.resize(screenSpec)
        window.host(view)
        return window.windowContext
    }

    private fun backgroundWindow(): BackgroundWindow =
        backgroundWindow ?: BackgroundWindow(appContext, screenSpec).also { backgroundWindow = it }

    private fun releaseBackgroundWindow() {
        backgroundWindow?.release()
        backgroundWindow = null
    }

    private fun onCarConnectionChanged(connected: Boolean) {
        val lost = carConnected && !connected
        carConnected = connected
        if (!lost) return
        Log.d(TAG, "car disconnected")
        onCarModeExited?.invoke()
    }

    private fun onCarModeExit() {
        Log.d(TAG, "car mode exited")
        onCarModeExited?.invoke()
    }

    companion object {
        private const val TAG = "WebViewExGearhead"
        private const val ANDROID_AUTO_PACKAGE = "com.google.android.projection.gearhead"
        fun config(searchUrl: (String) -> String) = WebViewExConfig(name = "car", searchUrl = searchUrl)
    }
}
