package proto.media.fiesta.features.domain.core.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class SteeringWheelSideTest {

    @Test
    fun autoFollowsWhatTheCarReports() {
        assertEquals(ScreenSide.LEFT, SteeringWheelSide.AUTO.resolve(ScreenSide.LEFT))
        assertEquals(ScreenSide.RIGHT, SteeringWheelSide.AUTO.resolve(ScreenSide.RIGHT))
    }

    @Test
    fun leftAndRightOverrideWhatTheCarReports() {
        assertEquals(ScreenSide.LEFT, SteeringWheelSide.LEFT.resolve(ScreenSide.RIGHT))
        assertEquals(ScreenSide.RIGHT, SteeringWheelSide.RIGHT.resolve(ScreenSide.LEFT))
    }

    @Test
    fun parsesStoredValuesAndFallsBackToAuto() {
        assertEquals(SteeringWheelSide.LEFT, SteeringWheelSide.parse("left"))
        assertEquals(SteeringWheelSide.RIGHT, SteeringWheelSide.parse("right"))
        assertEquals(SteeringWheelSide.AUTO, SteeringWheelSide.parse("auto"))
        assertEquals(SteeringWheelSide.AUTO, SteeringWheelSide.parse(null))
    }
}
