package proto.media.fiesta.features.domain.core.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import proto.media.fiesta.features.domain.core.settings.FullscreenControlsPlacement.Box

class FullscreenControlsPlacementTest {

    private val area = Box(0f, 0f, 800f, 400f)

    @Test
    fun clampKeepsAPositionThatAlreadyFits() {
        val box = FullscreenControlsPlacement.clampToArea(300f, 150f, 200f, 80f, area)
        assertEquals(300f, box.left, 0f)
        assertEquals(150f, box.top, 0f)
    }

    @Test
    fun clampPullsBackFromTheLeadingEdges() {
        val box = FullscreenControlsPlacement.clampToArea(-50f, -20f, 200f, 80f, area)
        assertEquals(0f, box.left, 0f)
        assertEquals(0f, box.top, 0f)
    }

    @Test
    fun clampPullsBackFromTheTrailingEdges() {
        val box = FullscreenControlsPlacement.clampToArea(750f, 380f, 200f, 80f, area)
        assertEquals(600f, box.left, 0f)
        assertEquals(320f, box.top, 0f)
    }

    @Test
    fun touchingBoxesDoNotOverlap() {
        val a = Box(0f, 0f, 100f, 100f)
        val b = Box(100f, 0f, 100f, 100f)
        assertFalse(FullscreenControlsPlacement.overlaps(a, b))
    }

    @Test
    fun overlappingBoxesAreDetected() {
        val a = Box(0f, 0f, 100f, 100f)
        val b = Box(50f, 50f, 100f, 100f)
        assertTrue(FullscreenControlsPlacement.overlaps(a, b))
    }

    @Test
    fun avoidOverlapReturnsTheSameBoxWhenFree() {
        val bar = Box(10f, 10f, 200f, 60f)
        val result = FullscreenControlsPlacement.avoidOverlap(bar, listOf(Box(400f, 300f, 100f, 60f)), area)
        assertEquals(bar, result)
    }

    @Test
    fun avoidOverlapJumpsClearOfAToolbar() {
        val bar = Box(0f, 0f, 200f, 60f)
        val toolbar = Box(0f, 0f, 300f, 400f)
        val result = FullscreenControlsPlacement.avoidOverlap(bar, listOf(toolbar), area)
        assertFalse(FullscreenControlsPlacement.overlaps(result, toolbar))
        assertTrue(result.left >= area.left && result.right <= area.right)
        assertTrue(result.top >= area.top && result.bottom <= area.bottom)
    }

    @Test
    fun avoidOverlapResolvesTwoObstaclesInSequence() {
        val bar = Box(340f, 160f, 120f, 80f)
        val bubble = Box(300f, 150f, 200f, 100f)
        val panel = Box(0f, 0f, 250f, 400f)
        val result = FullscreenControlsPlacement.avoidOverlap(bar, listOf(bubble, panel), area)
        assertFalse(FullscreenControlsPlacement.overlaps(result, bubble))
        assertFalse(FullscreenControlsPlacement.overlaps(result, panel))
    }

    @Test
    fun barNearTheTopLeavesThroughTheTop() {
        val offset = FullscreenControlsPlacement.offsetOutThroughNearestEdge(Box(300f, 20f, 200f, 80f), area)
        assertEquals(0f, offset.x, 0f)
        assertEquals(-100f, offset.y, 0f)
    }

    @Test
    fun barNearTheBottomLeavesThroughTheBottom() {
        val offset = FullscreenControlsPlacement.offsetOutThroughNearestEdge(Box(300f, 300f, 200f, 80f), area)
        assertEquals(0f, offset.x, 0f)
        assertEquals(100f, offset.y, 0f)
    }

    @Test
    fun barNearTheLeftLeavesThroughTheLeft() {
        val offset = FullscreenControlsPlacement.offsetOutThroughNearestEdge(Box(10f, 160f, 60f, 80f), area)
        assertEquals(-70f, offset.x, 0f)
        assertEquals(0f, offset.y, 0f)
    }

    @Test
    fun barNearTheRightLeavesThroughTheRight() {
        val offset = FullscreenControlsPlacement.offsetOutThroughNearestEdge(Box(730f, 160f, 60f, 80f), area)
        assertEquals(70f, offset.x, 0f)
        assertEquals(0f, offset.y, 0f)
    }
}
