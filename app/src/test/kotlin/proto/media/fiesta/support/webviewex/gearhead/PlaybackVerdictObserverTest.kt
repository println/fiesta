package proto.media.fiesta.support.webviewex.gearhead

import org.junit.Assert.assertEquals
import org.junit.Test
import proto.media.fiesta.support.webviewex.media.MediaElementEvent
import proto.media.fiesta.support.webviewex.media.MediaElementEvent.*
import proto.media.fiesta.support.webviewex.media.MediaElementState

class PlaybackVerdictObserverTest {

    private var now = 0L
    private val scheduled = mutableListOf<Pair<Long, () -> Unit>>()
    private val verdicts = mutableListOf<PlaybackVerdict>()
    private val observer = PlaybackVerdictObserver(
        clockMillis = { now },
        scheduleAfter = { delay, action -> scheduled += (now + delay) to action },
        onVerdictChanged = { snapshot, _ -> verdicts += snapshot.verdict }
    )

    private fun element(event: MediaElementEvent, position: Double, document: String = "doc-1") =
        observer.onMediaElementEvent(event, position, document)

    private fun advanceTo(millis: Long) {
        now = millis
        scheduled.filter { it.first <= millis }.forEach { (at, action) ->
            scheduled.remove(at to action)
            action()
        }
    }

    @Test
    fun windowSwitchTimelineStaysInterruptedUntilPlaybackReturns() {
        element(PLAYING, 140.0)
        now = 1_000
        element(EMPTIED, 0.0)
        element(LOAD_START, 0.0)
        now = 3_200
        element(LOADED_METADATA, 0.0)
        element(SEEKED, 140.7)
        element(PLAYING, 140.7)

        assertEquals(
            listOf(PlaybackVerdict.PLAYING, PlaybackVerdict.INTERRUPTED, PlaybackVerdict.PLAYING),
            verdicts
        )
        assertEquals(140.7, observer.snapshot.trustedPositionSeconds, 0.0)
    }

    @Test
    fun playerSelfPauseBetweenTracksNeverBecomesAPause() {
        element(PLAYING, 10.0)
        now = 1_000
        element(PAUSE, 15.0)
        now = 1_400
        element(LOAD_START, 0.0)
        now = 2_500
        element(PLAYING, 0.0)
        advanceTo(20_000)

        assertEquals(
            listOf(PlaybackVerdict.PLAYING, PlaybackVerdict.INTERRUPTED, PlaybackVerdict.PLAYING),
            verdicts
        )
    }

    @Test
    fun pageSelfPauseSettlesIntoAPauseWithoutAnyOtherEvent() {
        element(PLAYING, 10.0)
        now = 1_000
        element(PAUSE, 12.0)
        advanceTo(1_000 + PlaybackVerdictRules.PAGE_PAUSE_SETTLE_MILLIS + 1)

        assertEquals(
            listOf(PlaybackVerdict.PLAYING, PlaybackVerdict.INTERRUPTED, PlaybackVerdict.PAUSED_BY_PAGE),
            verdicts
        )
    }

    @Test
    fun lastTrackEndingSettlesIntoStopped() {
        element(PLAYING, 230.0)
        now = 2_000
        element(ENDED, 232.0)
        advanceTo(2_000 + PlaybackVerdictRules.MAX_INTERRUPTION_MILLIS + 1)

        assertEquals(PlaybackVerdict.STOPPED, verdicts.last())
    }

    @Test
    fun pauseCommandIsConfirmedAtOnce() {
        element(PLAYING, 30.0)
        observer.onCommand(ObservedCommand(ObservedCommandKind.PAUSE, "card", now))
        element(PAUSE, 30.5)

        assertEquals(listOf(PlaybackVerdict.PLAYING, PlaybackVerdict.PAUSED_BY_COMMAND), verdicts)
    }

    @Test
    fun newDocumentForgetsTheElementOfThePreviousOne() {
        element(PLAYING, 30.0, document = "doc-1")
        element(LOAD_START, 0.0, document = "doc-2")

        assertEquals(MediaElementState.LOADING, observer.snapshot.element)
        assertEquals(0.0, observer.snapshot.trustedPositionSeconds, 0.0)
    }

    @Test
    fun playbackIsExpectedWhilePlayingInterruptedOrRequestedBeforeAnyElement() {
        observer.onCommand(ObservedCommand(ObservedCommandKind.PLAY, "card", now))
        assertEquals(true, observer.snapshot.expectsPlayback)

        element(PLAYING, 1.0)
        assertEquals(true, observer.snapshot.expectsPlayback)

        element(EMPTIED, 0.0)
        assertEquals(true, observer.snapshot.expectsPlayback)
    }

    @Test
    fun pausedPlaybackIsNotExpected() {
        element(PLAYING, 1.0)
        observer.onCommand(ObservedCommand(ObservedCommandKind.PAUSE, "card", now))
        element(PAUSE, 1.5)

        assertEquals(false, observer.snapshot.expectsPlayback)
    }

    @Test
    fun destroyedViewHasNoElement() {
        element(PLAYING, 30.0)
        observer.onViewDestroyed()

        assertEquals(PlaybackVerdict.STOPPED, observer.snapshot.verdict)
    }
}
