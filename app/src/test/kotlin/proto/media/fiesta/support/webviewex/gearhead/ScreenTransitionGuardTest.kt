package proto.media.fiesta.support.webviewex.gearhead

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import proto.media.fiesta.support.webviewex.media.MediaElementEvent.*

class ScreenTransitionGuardTest {

    private var now = 0L
    private val guard = ScreenTransitionGuard({ now }, automaticPauseOrigin = GEARHEAD)

    private fun command(kind: ObservedCommandKind, origin: String) = ObservedCommand(kind, origin, now)

    @Test
    fun discardThenMetadataResumesFromSavedPosition() {
        guard.armIfPlaying(true, 140.7)

        assertEquals(GuardAction.None, guard.onEvent(EMPTIED, 0.0, 0.0))
        assertEquals(GuardAction.PlayFrom(140), guard.onEvent(LOADED_METADATA, 0.0, 0.0))
    }

    @Test
    fun resumesFromTheLatestTrustedPositionNotTheOneOfTheAttach() {
        guard.armIfPlaying(true, 198.0)
        guard.onEvent(TIME_UPDATE, 200.9, 200.9)
        guard.onEvent(EMPTIED, 0.0, 200.9)

        assertEquals(GuardAction.PlayFrom(200), guard.onEvent(LOADED_METADATA, 0.0, 200.9))
    }

    @Test
    fun slowReloadAfterTheDiscardStillResumes() {
        guard.armIfPlaying(true, 52.0)
        now = 1_800
        guard.onEvent(EMPTIED, 0.0, 52.0)
        now = 1_800 + ScreenTransitionGuard.WINDOW_MILLIS - 1

        assertEquals(GuardAction.Play, guard.onEvent(SEEKED, 52.0, 52.0))
    }

    @Test
    fun initialLoadAfterTheAttachDoesNotSpendTheGuardBeforeTheWindowSwitch() {
        guard.armIfPlaying(true, 0.0)
        guard.onEvent(EMPTIED, 0.0, 0.0)
        guard.onEvent(LOADED_METADATA, 0.0, 0.0)
        guard.onEvent(PLAYING, 0.0, 0.0)
        now = 1_500
        guard.onEvent(EMPTIED, 0.0, 0.0)

        assertEquals(GuardAction.Play, guard.onEvent(LOADED_METADATA, 0.0, 0.0))
    }

    @Test
    fun seekedAtSavedPositionJustPlays() {
        guard.armIfPlaying(true, 140.7)
        guard.onEvent(EMPTIED, 0.0, 0.0)

        assertEquals(GuardAction.Play, guard.onEvent(SEEKED, 140.7, 0.0))
    }

    @Test
    fun sizePauseWithoutDiscardPlaysAgain() {
        guard.armIfPlaying(true, 30.0)

        assertEquals(GuardAction.Play, guard.onEvent(PAUSE, 30.0, 0.0))
    }

    @Test
    fun automaticPauseOfTheAttachIsSwallowedAndPlaybackResumes() {
        guard.armIfPlaying(true, 65.3)
        guard.onEvent(EMPTIED, 0.0, 0.0)
        now = 550

        assertFalse(guard.admits(command(ObservedCommandKind.PAUSE, GEARHEAD)))
        assertEquals(GuardAction.PlayFrom(65), guard.onEvent(LOADED_METADATA, 0.0, 0.0))
    }

    @Test
    fun secondPauseFromAndroidAutoIsTheUser() {
        guard.armIfPlaying(true, 30.0)
        guard.admits(command(ObservedCommandKind.PAUSE, GEARHEAD))

        assertTrue(guard.admits(command(ObservedCommandKind.PAUSE, GEARHEAD)))
        assertEquals(GuardAction.None, guard.onEvent(PAUSE, 30.0, 0.0))
    }

    @Test
    fun pauseFromAndroidAutoLongAfterTheAttachIsTheUser() {
        guard.armIfPlaying(true, 30.0)
        now = ScreenTransitionGuard.AUTOMATIC_PAUSE_WINDOW_MILLIS + 1

        assertTrue(guard.admits(command(ObservedCommandKind.PAUSE, GEARHEAD)))
        assertEquals(GuardAction.None, guard.onEvent(PAUSE, 30.0, 0.0))
    }

    @Test
    fun pauseFromAnotherOriginDisarms() {
        guard.armIfPlaying(true, 30.0)

        assertTrue(guard.admits(command(ObservedCommandKind.PAUSE, "com.android.systemui")))
        assertEquals(GuardAction.None, guard.onEvent(PAUSE, 30.0, 0.0))
    }

    @Test
    fun playCommandKeepsTheGuard() {
        guard.armIfPlaying(true, 30.0)

        assertTrue(guard.admits(command(ObservedCommandKind.PLAY, GEARHEAD)))
        assertEquals(GuardAction.Play, guard.onEvent(PAUSE, 30.0, 0.0))
    }

    @Test
    fun pauseAfterTheWindowIsARealPause() {
        guard.armIfPlaying(true, 30.0)
        now = ScreenTransitionGuard.WINDOW_MILLIS + 1

        assertTrue(guard.admits(command(ObservedCommandKind.PAUSE, GEARHEAD)))
        assertEquals(GuardAction.None, guard.onEvent(PAUSE, 30.0, 0.0))
    }

    @Test
    fun notPlayingBeforeAttachNeverResumes() {
        guard.armIfPlaying(false, 30.0)

        assertTrue(guard.admits(command(ObservedCommandKind.PAUSE, GEARHEAD)))
        assertEquals(GuardAction.None, guard.onEvent(PAUSE, 30.0, 0.0))
    }

    @Test
    fun resumeIsCappedToAvoidLoops() {
        guard.armIfPlaying(true, 30.0)
        repeat(3) { guard.onEvent(PAUSE, 30.0, 0.0) }

        assertEquals(GuardAction.None, guard.onEvent(PAUSE, 30.0, 0.0))
    }

    private companion object {
        const val GEARHEAD = "com.google.android.projection.gearhead"
    }
}
