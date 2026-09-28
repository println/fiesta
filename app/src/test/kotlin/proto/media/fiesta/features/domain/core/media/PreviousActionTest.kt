package proto.media.fiesta.features.domain.core.media

import org.junit.Assert.assertEquals
import org.junit.Test

class PreviousActionTest {

    @Test
    fun `restarts when past the threshold with a previous available`() {
        assertEquals(PreviousAction.RESTART, PreviousAction.decide(positionMillis = 3_001, hasPrevious = true))
    }

    @Test
    fun `goes to previous within the threshold when one is available`() {
        assertEquals(PreviousAction.PREVIOUS, PreviousAction.decide(positionMillis = 3_000, hasPrevious = true))
    }

    @Test
    fun `restarts within the threshold when there is no previous`() {
        assertEquals(PreviousAction.RESTART, PreviousAction.decide(positionMillis = 0, hasPrevious = false))
    }

    @Test
    fun `restarts past the threshold even without a previous`() {
        assertEquals(PreviousAction.RESTART, PreviousAction.decide(positionMillis = 10_000, hasPrevious = false))
    }
}
