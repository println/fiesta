package proto.media.fiesta.features.domain.core.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolbarPositionTest {

    @Test
    fun parsesEachExplicitValueRegardlessOfDriverSide() {
        for (driverSide in ScreenSide.values()) {
            assertEquals(ToolbarPosition.LEFT, ToolbarPosition.parse("left", driverSide))
            assertEquals(ToolbarPosition.RIGHT, ToolbarPosition.parse("right", driverSide))
            assertEquals(ToolbarPosition.TOP, ToolbarPosition.parse("top", driverSide))
            assertEquals(ToolbarPosition.BOTTOM, ToolbarPosition.parse("bottom", driverSide))
        }
    }

    @Test
    fun driverSideFollowsTheSteeringWheel() {
        assertEquals(ToolbarPosition.LEFT, ToolbarPosition.parse("driver", ScreenSide.LEFT))
        assertEquals(ToolbarPosition.RIGHT, ToolbarPosition.parse("driver", ScreenSide.RIGHT))
    }

    @Test
    fun unknownOrNullValueFollowsTheSteeringWheel() {
        assertEquals(ToolbarPosition.LEFT, ToolbarPosition.parse("diagonal", ScreenSide.LEFT))
        assertEquals(ToolbarPosition.RIGHT, ToolbarPosition.parse(null, ScreenSide.RIGHT))
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

    @Test
    fun verticalBarKeepsSettingsPanelOnItsSideAndFavoritesOnTheOther() {
        for (driverSide in ScreenSide.values()) {
            assertEquals(ScreenSide.LEFT, ToolbarPosition.LEFT.settingsPanelSide(driverSide))
            assertEquals(ScreenSide.RIGHT, ToolbarPosition.LEFT.favoritesBarSide(driverSide))
            assertEquals(ScreenSide.RIGHT, ToolbarPosition.RIGHT.settingsPanelSide(driverSide))
            assertEquals(ScreenSide.LEFT, ToolbarPosition.RIGHT.favoritesBarSide(driverSide))
        }
    }

    @Test
    fun horizontalBarPutsSettingsPanelOnDriverSideAndFavoritesOnPassengerSide() {
        for (position in listOf(ToolbarPosition.TOP, ToolbarPosition.BOTTOM)) {
            assertEquals(ScreenSide.LEFT, position.settingsPanelSide(ScreenSide.LEFT))
            assertEquals(ScreenSide.RIGHT, position.favoritesBarSide(ScreenSide.LEFT))
            assertEquals(ScreenSide.RIGHT, position.settingsPanelSide(ScreenSide.RIGHT))
            assertEquals(ScreenSide.LEFT, position.favoritesBarSide(ScreenSide.RIGHT))
        }
    }
}
