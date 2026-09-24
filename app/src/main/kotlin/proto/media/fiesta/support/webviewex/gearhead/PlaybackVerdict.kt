package proto.media.fiesta.support.webviewex.gearhead

import proto.media.fiesta.support.webviewex.media.MediaElementState

enum class PlaybackVerdict { PLAYING, PAUSED_BY_COMMAND, PAUSED_BY_PAGE, INTERRUPTED, STOPPED }

data class PlaybackEvidence(
    val element: MediaElementState,
    val elementSinceMillis: Long,
    val audioOutput: AudioOutputState?,
    val lastCommand: ObservedCommand?,
    val nowMillis: Long
)

object PlaybackVerdictRules {

    const val COMMAND_WINDOW_MILLIS = 3_000L
    const val PAGE_PAUSE_SETTLE_MILLIS = 2_000L
    const val MAX_INTERRUPTION_MILLIS = 10_000L

    fun decide(evidence: PlaybackEvidence): PlaybackVerdict {
        val element = evidence.element
        return when {
            element == MediaElementState.NONE -> PlaybackVerdict.STOPPED
            element == MediaElementState.PLAYING -> PlaybackVerdict.PLAYING
            pausedByRecentCommand(evidence) -> PlaybackVerdict.PAUSED_BY_COMMAND
            evidence.audioOutput == AudioOutputState.FOCUS_LOST -> PlaybackVerdict.PAUSED_BY_PAGE
            element == MediaElementState.PAUSED ->
                if (lastedLessThan(evidence, PAGE_PAUSE_SETTLE_MILLIS)) PlaybackVerdict.INTERRUPTED
                else PlaybackVerdict.PAUSED_BY_PAGE
            element == MediaElementState.ENDED ->
                if (lastedLessThan(evidence, MAX_INTERRUPTION_MILLIS)) PlaybackVerdict.INTERRUPTED
                else PlaybackVerdict.STOPPED
            else ->
                if (lastedLessThan(evidence, MAX_INTERRUPTION_MILLIS)) PlaybackVerdict.INTERRUPTED
                else PlaybackVerdict.PAUSED_BY_PAGE
        }
    }

    fun settlesAtMillis(evidence: PlaybackEvidence): Long? = when (evidence.element) {
        MediaElementState.PAUSED -> evidence.elementSinceMillis + PAGE_PAUSE_SETTLE_MILLIS
        MediaElementState.ENDED, MediaElementState.LOADING, MediaElementState.EMPTIED ->
            evidence.elementSinceMillis + MAX_INTERRUPTION_MILLIS
        else -> null
    }

    private fun lastedLessThan(evidence: PlaybackEvidence, millis: Long) =
        evidence.nowMillis - evidence.elementSinceMillis < millis

    private fun pausedByRecentCommand(evidence: PlaybackEvidence): Boolean {
        val command = evidence.lastCommand ?: return false
        val pausing = command.kind == ObservedCommandKind.PAUSE || command.kind == ObservedCommandKind.STOP
        return pausing && evidence.nowMillis - command.atMillis <= COMMAND_WINDOW_MILLIS
    }
}
