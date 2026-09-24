package proto.media.fiesta.features.domain.core.settings

object ZoomStep {

    const val STEP = 10
    const val MIN = 50
    const val MAX = 200
    const val DEFAULT = 100

    fun next(current: Int, direction: Int): Int {
        val target = if (direction >= 0) {
            ceilToStep(current) + if (isOnStep(current)) STEP else 0
        } else {
            floorToStep(current) - if (isOnStep(current)) STEP else 0
        }
        return target.coerceIn(MIN, MAX)
    }

    private fun isOnStep(value: Int) = value % STEP == 0

    private fun ceilToStep(value: Int) = ((value + STEP - 1) / STEP) * STEP

    private fun floorToStep(value: Int) = (value / STEP) * STEP
}
