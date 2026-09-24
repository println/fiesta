package proto.media.fiesta.features.domain.core.media

enum class PreviousAction {
    RESTART, PREVIOUS;

    companion object {
        const val RESTART_THRESHOLD_MILLIS = 3_000L

        fun decide(positionMillis: Long, hasPrevious: Boolean): PreviousAction =
            if (positionMillis > RESTART_THRESHOLD_MILLIS || !hasPrevious) RESTART else PREVIOUS
    }
}
