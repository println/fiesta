package proto.media.fiesta.features.domain.core.settings

enum class ToolbarPosition {
    LEFT, RIGHT, TOP, BOTTOM;

    val isVertical: Boolean get() = this == LEFT || this == RIGHT

    val barFirst: Boolean get() = this == LEFT || this == TOP

    companion object {
        const val VALUE_LEFT = "left"
        const val VALUE_RIGHT = "right"
        const val VALUE_TOP = "top"
        const val VALUE_BOTTOM = "bottom"

        fun parse(value: String?): ToolbarPosition = when (value) {
            VALUE_RIGHT -> RIGHT
            VALUE_TOP -> TOP
            VALUE_BOTTOM -> BOTTOM
            else -> LEFT
        }
    }
}
