package proto.media.fiesta.support.webviewex.gearhead

import proto.media.fiesta.support.webviewex.media.MediaElementEvent
import proto.media.fiesta.support.webviewex.media.MediaElementState
import proto.media.fiesta.support.webviewex.media.MediaElementTracker

data class PlaybackSnapshot(
    val verdict: PlaybackVerdict,
    val element: MediaElementState,
    val trustedPositionSeconds: Double,
    val audioOutput: AudioOutputState?,
    val lastCommand: ObservedCommand?
) {
    val expectsPlayback: Boolean
        get() = verdict == PlaybackVerdict.PLAYING ||
            verdict == PlaybackVerdict.INTERRUPTED ||
            (element == MediaElementState.NONE && lastCommand?.kind == ObservedCommandKind.PLAY)
}

class PlaybackVerdictObserver(
    private val clockMillis: () -> Long,
    private val scheduleAfter: (Long, () -> Unit) -> Unit,
    private val onVerdictChanged: (PlaybackSnapshot, String) -> Unit
) {

    private val tracker = MediaElementTracker()
    private var documentId: String? = null
    private var elementSinceMillis = 0L
    private var audioState: AudioOutputState? = null
    private var lastCommand: ObservedCommand? = null
    private var lastVerdict: PlaybackVerdict? = null
    private var settlingScheduledAt: Long? = null

    val snapshot: PlaybackSnapshot
        get() {
            val evidence = evidence()
            return PlaybackSnapshot(
                PlaybackVerdictRules.decide(evidence),
                tracker.state,
                tracker.trustedPositionSeconds,
                audioState,
                lastCommand
            )
        }

    fun onAudioOutput(state: AudioOutputState) {
        audioState = state
        evaluate("audio=$state")
    }

    fun onCommand(command: ObservedCommand) {
        lastCommand = command
        evaluate("command=${command.kind} from ${command.origin}")
    }

    fun onMediaElementEvent(event: MediaElementEvent, positionSeconds: Double, documentId: String) {
        if (documentId != this.documentId) {
            this.documentId = documentId
            tracker.reset()
        }
        val before = tracker.state
        tracker.onEvent(event, positionSeconds)
        if (tracker.state != before) elementSinceMillis = clockMillis()
        evaluate("element=$event at $positionSeconds")
    }

    fun onViewDestroyed() {
        documentId = null
        tracker.reset()
        elementSinceMillis = clockMillis()
        evaluate("view destroyed")
    }

    private fun evidence() = PlaybackEvidence(tracker.state, elementSinceMillis, audioState, lastCommand, clockMillis())

    private fun evaluate(cause: String) {
        val current = snapshot
        scheduleSettling()
        if (current.verdict == lastVerdict) return
        lastVerdict = current.verdict
        onVerdictChanged(current, cause)
    }

    private fun scheduleSettling() {
        val evidence = evidence()
        val at = PlaybackVerdictRules.settlesAtMillis(evidence) ?: return
        if (at < evidence.nowMillis || at == settlingScheduledAt) return
        settlingScheduledAt = at
        scheduleAfter(at - evidence.nowMillis + 1) {
            if (settlingScheduledAt == at) evaluate("settled")
        }
    }
}
