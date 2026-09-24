package proto.media.fiesta.features.domain.core.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolbarPositionTest {

    @Test
    fun parsesEachKnownValue() {
        assertEquals(ToolbarPosition.LEFT, ToolbarPosition.parse("left"))
        assertEquals(ToolbarPosition.RIGHT, ToolbarPosition.parse("right"))
        assertEquals(ToolbarPosition.TOP, ToolbarPosition.parse("top"))
        assertEquals(ToolbarPosition.BOTTOM, ToolbarPosition.parse("bottom"))
    }

    @Test
    fun fallsBackToLeftForUnknownOrNullValue() {
        assertEquals(ToolbarPosition.LEFT, ToolbarPosition.parse("diagonal"))
        assertEquals(ToolbarPosition.LEFT, ToolbarPosition.parse(null))
    }

    @Test
    fun leftAndRightAreVertical() {
        assertTrue(ToolbarPosition.LEFT.isVertical)
        assertTrue(ToolbarPosition.RIGHT.isVertical)
        assertFalse(ToolbarPosition.TOP.isVertical)
        assertFalse(ToolbarPosition.BOTTOM.isVertical)
    }

    @Test
    fun leftAndTopComeFirst() {
        assertTrue(ToolbarPosition.LEFT.barFirst)
        assertTrue(ToolbarPosition.TOP.barFirst)
        assertFalse(ToolbarPosition.RIGHT.barFirst)
        assertFalse(ToolbarPosition.BOTTOM.barFirst)
    }
}
