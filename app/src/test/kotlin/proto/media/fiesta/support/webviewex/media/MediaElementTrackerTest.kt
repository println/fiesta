package proto.media.fiesta.support.webviewex.media

import org.junit.Assert.assertEquals
import org.junit.Test
import proto.media.fiesta.support.webviewex.media.MediaElementEvent.*

class MediaElementTrackerTest {

    private val tracker = MediaElementTracker()

    @Test
    fun startsWithNoElement() {
        assertEquals(MediaElementState.NONE, tracker.state)
    }

    @Test
    fun playingThenPauseIsPausedAtThatPosition() {
        tracker.onEvent(PLAYING, 12.0)
        tracker.onEvent(PAUSE, 15.0)

        assertEquals(MediaElementState.PAUSED, tracker.state)
        assertEquals(15.0, tracker.trustedPositionSeconds, 0.0)
    }

    @Test
    fun emptiedKeepsThePositionFromBeforeTheDiscard() {
        tracker.onEvent(TIME_UPDATE, 140.7)
        tracker.onEvent(EMPTIED, 0.0)
        tracker.onEvent(LOAD_START, 0.0)
        tracker.onEvent(TIME_UPDATE, 0.0)

        assertEquals(MediaElementState.LOADING, tracker.state)
        assertEquals(140.7, tracker.trustedPositionSeconds, 0.0)
    }

    @Test
    fun seekedAfterReloadRestoresTrustedPosition() {
        tracker.onEvent(TIME_UPDATE, 140.7)
        tracker.onEvent(EMPTIED, 0.0)
        tracker.onEvent(LOAD_START, 0.0)
        tracker.onEvent(LOADED_METADATA, 0.0)
        tracker.onEvent(SEEKED, 140.7)

        assertEquals(140.7, tracker.trustedPositionSeconds, 0.0)
    }

    @Test
    fun pauseAfterEndedStaysEnded() {
        tracker.onEvent(ENDED, 232.0)
        tracker.onEvent(PAUSE, 232.0)

        assertEquals(MediaElementState.ENDED, tracker.state)
    }

    @Test
    fun unknownJsEventIsIgnored() {
        assertEquals(null, MediaElementEvent.fromJs("volumechange"))
        assertEquals(PLAYING, MediaElementEvent.fromJs("playing"))
    }
}
