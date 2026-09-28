package proto.media.fiesta.features.domain.core.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class ZoomStepTest {

    @Test
    fun stepsUpByTen() {
        assertEquals(90, ZoomStep.next(80, +1))
    }

    @Test
    fun stepsDownByTen() {
        assertEquals(70, ZoomStep.next(80, -1))
    }

    @Test
    fun stepsUpPastOldCeiling() {
        assertEquals(110, ZoomStep.next(100, +1))
    }

    @Test
    fun clampsAtUpperLimit() {
        assertEquals(ZoomStep.MAX, ZoomStep.next(ZoomStep.MAX, +1))
    }

    @Test
    fun roundsOffUpperStepValueUpToLimit() {
        assertEquals(110, ZoomStep.next(105, +1))
    }

    @Test
    fun clampsAtLowerLimit() {
        assertEquals(50, ZoomStep.next(50, -1))
    }

    @Test
    fun roundsOffStepValueUpToNextStep() {
        assertEquals(80, ZoomStep.next(75, +1))
    }

    @Test
    fun roundsOffStepValueDownToPreviousStep() {
        assertEquals(70, ZoomStep.next(75, -1))
    }
}
