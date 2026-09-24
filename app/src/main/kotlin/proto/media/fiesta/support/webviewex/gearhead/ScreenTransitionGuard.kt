package proto.media.fiesta.support.webviewex.gearhead

import proto.media.fiesta.support.webviewex.media.MediaElementEvent

sealed class GuardAction {
    data object None : GuardAction()
    data object Play : GuardAction()
    data class PlayFrom(val seconds: Int) : GuardAction()
}

class ScreenTransitionGuard(
    private val clockMillis: () -> Long,
    private val automaticPauseOrigin: String,
    private val windowMillis: Long = WINDOW_MILLIS
) {

    private var armed = false
    private var armedAtMillis = 0L
    private var windowStartMillis = 0L
    private var savedPositionSeconds = 0.0
    private var discarded = false
    private var automaticPauseSwallowed = false
    private var actions = 0

    private val isArmed: Boolean
        get() = armed && clockMillis() - windowStartMillis <= windowMillis

    fun armIfPlaying(playing: Boolean, positionSeconds: Double) {
        armed = playing
        armedAtMillis = clockMillis()
        windowStartMillis = armedAtMillis
        savedPositionSeconds = positionSeconds
        discarded = false
        automaticPauseSwallowed = false
        actions = 0
    }

    fun admits(command: ObservedCommand): Boolean {
        if (!isArmed) return true
        if (isAutomaticPauseOnAttach(command)) {
            automaticPauseSwallowed = true
            return false
        }
        if (command.kind == ObservedCommandKind.PAUSE || command.kind == ObservedCommandKind.STOP) armed = false
        return true
    }

    fun onEvent(event: MediaElementEvent, positionSeconds: Double, trustedPositionSeconds: Double): GuardAction {
        if (!isArmed) {
            armed = false
            return GuardAction.None
        }
        if (trustedPositionSeconds > 0) savedPositionSeconds = trustedPositionSeconds
        return when (event) {
            MediaElementEvent.EMPTIED -> {
                discarded = true
                windowStartMillis = clockMillis()
                GuardAction.None
            }
            MediaElementEvent.PAUSE -> if (discarded) GuardAction.None else resume(positionSeconds)
            MediaElementEvent.LOADED_METADATA, MediaElementEvent.SEEKED ->
                if (discarded) resume(positionSeconds) else GuardAction.None
            else -> GuardAction.None
        }
    }

    private fun isAutomaticPauseOnAttach(command: ObservedCommand): Boolean =
        command.kind == ObservedCommandKind.PAUSE &&
            command.origin == automaticPauseOrigin &&
            !automaticPauseSwallowed &&
            command.atMillis - armedAtMillis <= AUTOMATIC_PAUSE_WINDOW_MILLIS

    private fun resume(currentPositionSeconds: Double): GuardAction {
        if (actions >= MAX_ACTIONS) return GuardAction.None
        actions++
        val wentBack = currentPositionSeconds + SEEK_TOLERANCE_SECONDS < savedPositionSeconds
        return if (wentBack) GuardAction.PlayFrom(savedPositionSeconds.toInt()) else GuardAction.Play
    }

    companion object {
        const val WINDOW_MILLIS = 5_000L
        const val AUTOMATIC_PAUSE_WINDOW_MILLIS = 2_000L
        private const val MAX_ACTIONS = 3
        private const val SEEK_TOLERANCE_SECONDS = 2.0
    }
}
