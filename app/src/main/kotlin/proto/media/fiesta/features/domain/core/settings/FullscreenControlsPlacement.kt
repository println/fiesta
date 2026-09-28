package proto.media.fiesta.features.domain.core.settings

import kotlin.math.hypot

object FullscreenControlsPlacement {

    data class Box(val left: Float, val top: Float, val width: Float, val height: Float) {
        val right: Float get() = left + width
        val bottom: Float get() = top + height
    }

    fun clampToArea(left: Float, top: Float, width: Float, height: Float, area: Box): Box = Box(
        left.coerceIn(area.left, (area.right - width).coerceAtLeast(area.left)),
        top.coerceIn(area.top, (area.bottom - height).coerceAtLeast(area.top)),
        width,
        height
    )

    fun overlaps(a: Box, b: Box): Boolean =
        a.left < b.right && a.right > b.left && a.top < b.bottom && a.bottom > b.top

    fun avoidOverlap(bar: Box, obstacles: List<Box>, area: Box): Box {
        var candidate = bar
        repeat(MAX_RESOLUTION_PASSES) {
            val hit = obstacles.firstOrNull { overlaps(candidate, it) } ?: return candidate
            candidate = nearestFreeSpot(candidate, hit, area)
        }
        return candidate
    }

    data class Offset(val x: Float, val y: Float)

    fun offsetOutThroughNearestEdge(bar: Box, area: Box): Offset {
        val toTop = bar.bottom - area.top
        val toBottom = area.bottom - bar.top
        val toLeft = bar.right - area.left
        val toRight = area.right - bar.left
        return when (minOf(toTop, toBottom, toLeft, toRight)) {
            toTop -> Offset(0f, -toTop)
            toBottom -> Offset(0f, toBottom)
            toLeft -> Offset(-toLeft, 0f)
            else -> Offset(toRight, 0f)
        }
    }

    private fun nearestFreeSpot(bar: Box, obstacle: Box, area: Box): Box {
        val sideSteps = listOf(
            clampToArea(obstacle.left - bar.width, bar.top, bar.width, bar.height, area),
            clampToArea(obstacle.right, bar.top, bar.width, bar.height, area),
            clampToArea(bar.left, obstacle.top - bar.height, bar.width, bar.height, area),
            clampToArea(bar.left, obstacle.bottom, bar.width, bar.height, area)
        )
        return sideSteps.filterNot { overlaps(it, obstacle) }.minByOrNull { distance(it, bar) } ?: bar
    }

    private fun distance(a: Box, b: Box): Float =
        hypot((a.left - b.left).toDouble(), (a.top - b.top).toDouble()).toFloat()

    private const val MAX_RESOLUTION_PASSES = 8
}
