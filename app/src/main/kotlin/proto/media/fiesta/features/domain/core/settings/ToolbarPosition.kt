package proto.media.fiesta.features.domain.core.settings

enum class ToolbarPosition {
    LEFT, RIGHT, TOP, BOTTOM;

    val isVertical: Boolean get() = this == LEFT || this == RIGHT

    val barFirst: Boolean get() = this == LEFT || this == TOP

    fun settingsPanelSide(driverSide: ScreenSide): ScreenSide = when (this) {
        LEFT -> ScreenSide.LEFT
        RIGHT -> ScreenSide.RIGHT
        TOP, BOTTOM -> driverSide
    }

    fun favoritesBarSide(driverSide: ScreenSide): ScreenSide = when (this) {
        LEFT -> ScreenSide.RIGHT
        RIGHT -> ScreenSide.LEFT
        TOP, BOTTOM -> driverSide.opposite
    }

    companion object {
        const val VALUE_DRIVER_SIDE = "driver"
        const val VALUE_LEFT = "left"
        const val VALUE_RIGHT = "right"
        const val VALUE_TOP = "top"
        const val VALUE_BOTTOM = "bottom"

        fun parse(value: String?, driverSide: ScreenSide): ToolbarPosition = when (value) {
            VALUE_LEFT -> LEFT
            VALUE_RIGHT -> RIGHT
            VALUE_TOP -> TOP
            VALUE_BOTTOM -> BOTTOM
            else -> if (driverSide == ScreenSide.RIGHT) RIGHT else LEFT
        }
    }
}
