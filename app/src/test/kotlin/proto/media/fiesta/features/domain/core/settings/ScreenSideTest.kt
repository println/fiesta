package proto.media.fiesta.features.domain.core.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenSideTest {

    @Test
    fun oppositeSwapsSides() {
        assertEquals(ScreenSide.RIGHT, ScreenSide.LEFT.opposite)
        assertEquals(ScreenSide.LEFT, ScreenSide.RIGHT.opposite)
    }

    @Test
    fun parsesRightAndFallsBackToLeft() {
        assertEquals(ScreenSide.RIGHT, ScreenSide.parse("right"))
        assertEquals(ScreenSide.LEFT, ScreenSide.parse("left"))
        assertEquals(ScreenSide.LEFT, ScreenSide.parse(null))
    }
}
