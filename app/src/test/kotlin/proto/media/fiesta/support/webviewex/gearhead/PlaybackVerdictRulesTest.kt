package proto.media.fiesta.support.webviewex.gearhead

import org.junit.Assert.assertEquals
import org.junit.Test
import proto.media.fiesta.support.webviewex.gearhead.PlaybackVerdictRules.MAX_INTERRUPTION_MILLIS
import proto.media.fiesta.support.webviewex.gearhead.PlaybackVerdictRules.PAGE_PAUSE_SETTLE_MILLIS
import proto.media.fiesta.support.webviewex.media.MediaElementState

class PlaybackVerdictRulesTest {

    private val now = 100_000L

    private fun decide(
        element: MediaElementState,
        forMillis: Long = 0,
        audio: AudioOutputState? = null,
        command: ObservedCommand? = null
    ) = PlaybackVerdictRules.decide(PlaybackEvidence(element, now - forMillis, audio, command, now))

    private fun pauseFrom(origin: String, agoMillis: Long) =
        ObservedCommand(ObservedCommandKind.PAUSE, origin, now - agoMillis)

    @Test
    fun noElementIsStopped() = assertEquals(PlaybackVerdict.STOPPED, decide(MediaElementState.NONE))

    @Test
    fun playingElementIsPlaying() = assertEquals(PlaybackVerdict.PLAYING, decide(MediaElementState.PLAYING))

    @Test
    fun emptiedOnWindowSwitchIsInterrupted() =
        assertEquals(PlaybackVerdict.INTERRUPTED, decide(MediaElementState.EMPTIED, forMillis = 2_200))

    @Test
    fun loadingAfterAdOrReloadIsInterrupted() =
        assertEquals(PlaybackVerdict.INTERRUPTED, decide(MediaElementState.LOADING, forMillis = 1_000))

    @Test
    fun loadingThatNeverEndsBecomesAPause() = assertEquals(
        PlaybackVerdict.PAUSED_BY_PAGE,
        decide(MediaElementState.LOADING, forMillis = MAX_INTERRUPTION_MILLIS)
    )

    @Test
    fun endedIsInterruptedWhileNextTrackArrives() =
        assertEquals(PlaybackVerdict.INTERRUPTED, decide(MediaElementState.ENDED, forMillis = 500))

    @Test
    fun endedWithoutNextTrackIsStopped() =
        assertEquals(PlaybackVerdict.STOPPED, decide(MediaElementState.ENDED, forMillis = MAX_INTERRUPTION_MILLIS))

    @Test
    fun pageSelfPauseIsInterruptedUntilItSettles() =
        assertEquals(PlaybackVerdict.INTERRUPTED, decide(MediaElementState.PAUSED, forMillis = 300))

    @Test
    fun settledPageSelfPauseIsPausedByPage() = assertEquals(
        PlaybackVerdict.PAUSED_BY_PAGE,
        decide(MediaElementState.PAUSED, forMillis = PAGE_PAUSE_SETTLE_MILLIS)
    )

    @Test
    fun pausedRightAfterPauseCommandIsPausedByCommand() = assertEquals(
        PlaybackVerdict.PAUSED_BY_COMMAND,
        decide(MediaElementState.PAUSED, command = pauseFrom("card", agoMillis = 400))
    )

    @Test
    fun pauseCommandDuringLoadingIsPausedByCommand() = assertEquals(
        PlaybackVerdict.PAUSED_BY_COMMAND,
        decide(MediaElementState.LOADING, command = pauseFrom("card", agoMillis = 400))
    )

    @Test
    fun staleCommandDoesNotExplainThePause() = assertEquals(
        PlaybackVerdict.INTERRUPTED,
        decide(MediaElementState.PAUSED, command = pauseFrom("card", agoMillis = 9_000))
    )

    @Test
    fun playCommandDoesNotExplainAPause() = assertEquals(
        PlaybackVerdict.INTERRUPTED,
        decide(MediaElementState.PAUSED, command = ObservedCommand(ObservedCommandKind.PLAY, "card", now - 400))
    )

    @Test
    fun lostAudioFocusIsARealPause() =
        assertEquals(PlaybackVerdict.PAUSED_BY_PAGE, decide(MediaElementState.PAUSED, audio = AudioOutputState.FOCUS_LOST))

    @Test
    fun outputStillFlowingDoesNotHideAPause() = assertEquals(
        PlaybackVerdict.PAUSED_BY_PAGE,
        decide(MediaElementState.PAUSED, forMillis = PAGE_PAUSE_SETTLE_MILLIS, audio = AudioOutputState.ACTIVE)
    )

    @Test
    fun settlingTimeFollowsTheElementState() {
        val since = 50_000L
        fun settles(element: MediaElementState) =
            PlaybackVerdictRules.settlesAtMillis(PlaybackEvidence(element, since, null, null, since))

        assertEquals(since + PAGE_PAUSE_SETTLE_MILLIS, settles(MediaElementState.PAUSED))
        assertEquals(since + MAX_INTERRUPTION_MILLIS, settles(MediaElementState.EMPTIED))
        assertEquals(null, settles(MediaElementState.PLAYING))
    }
}
