package proto.media.fiesta.features.domain.core.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CarMenuBubblePlacementTest {

    @Test
    fun fractionSurvivesAScreenOfDifferentSize() {
        val fraction = CarMenuBubblePlacement.toFraction(400f, 800)
        assertEquals(960f, CarMenuBubblePlacement.toOffset(fraction, 1920), 0.001f)
    }

    @Test
    fun fractionIsZeroBeforeTheParentHasASize() {
        assertEquals(0f, CarMenuBubblePlacement.toFraction(400f, 0), 0f)
    }

    @Test
    fun fitsWhenTheWholeBubbleIsOnScreen() {
        assertTrue(CarMenuBubblePlacement.fitsInside(716f, 316f, 84, 84, 800, 400))
    }

    @Test
    fun doesNotFitWhenAnyEdgeLeavesTheScreen() {
        assertFalse(CarMenuBubblePlacement.fitsInside(717f, 0f, 84, 84, 800, 400))
        assertFalse(CarMenuBubblePlacement.fitsInside(0f, 317f, 84, 84, 800, 400))
        assertFalse(CarMenuBubblePlacement.fitsInside(-1f, 0f, 84, 84, 800, 400))
        assertFalse(CarMenuBubblePlacement.fitsInside(0f, -1f, 84, 84, 800, 400))
    }
}
