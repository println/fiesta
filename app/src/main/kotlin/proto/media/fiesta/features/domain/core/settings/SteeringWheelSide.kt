package proto.media.fiesta.features.domain.core.settings

enum class SteeringWheelSide(val value: String) {
    AUTO("auto"), LEFT("left"), RIGHT("right");

    fun resolve(reportedByCar: ScreenSide): ScreenSide = when (this) {
        AUTO -> reportedByCar
        LEFT -> ScreenSide.LEFT
        RIGHT -> ScreenSide.RIGHT
    }

    companion object {
        fun parse(value: String?): SteeringWheelSide = values().firstOrNull { it.value == value } ?: AUTO
    }
}
