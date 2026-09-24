package proto.media.fiesta.support.webviewex.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaSessionCommandsTest {

    @Test
    fun clampSecondsRefusesNegativePositions() {
        assertEquals(0, MediaSessionCommands.clampSeconds(-5))
        assertEquals(5, MediaSessionCommands.clampSeconds(5))
    }

    @Test
    fun seekToNeverEmitsANegativeArgument() {
        assertTrue(MediaSessionCommands.seekTo(-5).contains("seekTo(0)"))
    }

    @Test
    fun seekByKeepsTheSignForRelativeSeeks() {
        assertTrue(MediaSessionCommands.seekBy(-10).contains("seekBy(-10)"))
        assertTrue(MediaSessionCommands.seekBy(10).contains("seekBy(10)"))
    }

    @Test
    fun playAndPauseCallTheBootstrapObjectWithNoArgument() {
        assertTrue(MediaSessionCommands.play().contains("__webviewexMedia && window.__webviewexMedia.play();"))
        assertTrue(MediaSessionCommands.pause().contains("__webviewexMedia && window.__webviewexMedia.pause();"))
    }

    @Test
    fun observeAndInterruptionCommandsTargetTheirBootstrapMethods() {
        assertTrue(MediaSessionCommands.observe().contains(".observe();"))
        assertTrue(MediaSessionCommands.markInterrupted().contains(".markInterrupted();"))
        assertTrue(MediaSessionCommands.resumeIfInterrupted().contains(".resumeIfInterrupted();"))
    }
}
