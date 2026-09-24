package proto.media.fiesta.features.domain.car.player

import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log

// The head unit takes media focus permanently (AUDIO_FOCUS_STATE_LOSS) for the lane camera and the
// like. The WebView owns the focus request and abandons it on the loss, so asking for focus is the
// only way to learn it is free again. Two rules keep the probe from harming whoever is playing:
// it asks to duck rather than to gain, because a gain of ours wins over a transient holder such as
// the assistant and turns the WebView's recoverable loss into a permanent one; and a probe granted
// straight away means nobody took the focus, so it is dropped without resuming anything.
class AudioFocusRegain(private val context: Context, private val onRegained: () -> Unit) {

    private val handler = Handler(Looper.getMainLooper())
    private val listener = AudioManager.OnAudioFocusChangeListener {}
    private val retry = Runnable { attempt() }
    private var deadline = 0L

    var isWaiting = false
        private set

    fun startIfFocusWasTaken() {
        if (isWaiting) return
        if (probeFocus()) {
            releaseFocus()
            return
        }
        Log.d(TAG, "focus is held by someone else, waiting for it to come back")
        isWaiting = true
        deadline = SystemClock.elapsedRealtime() + WAIT_LIMIT_MILLIS
        handler.postDelayed(retry, RETRY_MILLIS)
    }

    fun cancel() {
        isWaiting = false
        handler.removeCallbacks(retry)
    }

    private fun attempt() {
        if (!isWaiting) return
        if (probeFocus()) {
            releaseFocus()
            cancel()
            Log.d(TAG, "focus is free again, resuming")
            onRegained()
            return
        }
        if (SystemClock.elapsedRealtime() >= deadline) {
            Log.d(TAG, "gave up waiting for audio focus")
            cancel()
            return
        }
        handler.postDelayed(retry, RETRY_MILLIS)
    }

    @Suppress("DEPRECATION")
    private fun probeFocus(): Boolean =
        audioManager().requestAudioFocus(
            listener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
        ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED

    @Suppress("DEPRECATION")
    private fun releaseFocus() {
        audioManager().abandonAudioFocus(listener)
    }

    private fun audioManager(): AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    companion object {
        private const val TAG = "CarPlayer"
        private const val RETRY_MILLIS = 2_000L
        private const val WAIT_LIMIT_MILLIS = 120_000L
    }
}
