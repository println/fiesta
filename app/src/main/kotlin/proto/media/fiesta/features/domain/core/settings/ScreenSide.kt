package proto.media.fiesta.features.domain.core.settings

enum class ScreenSide(val value: String) {
    LEFT("left"), RIGHT("right");

    val opposite: ScreenSide get() = if (this == LEFT) RIGHT else LEFT

    companion object {
        fun parse(value: String?): ScreenSide = values().firstOrNull { it.value == value } ?: LEFT
    }
}
