package proto.media.fiesta.support.media.dto

enum class MediaAction {
    PLAY,
    PAUSE,
    STOP,
    FAST_FORWARD,
    REWIND,
    PLAY_FROM_SEARCH,
    SKIP_TO_NEXT,
    SKIP_TO_PREVIOUS,
    SEEK_TO,
    SKIP_TO_QUEUE_ITEM;

    companion object {
        val ALWAYS_AVAILABLE = setOf(PLAY, PAUSE, STOP, FAST_FORWARD, REWIND, PLAY_FROM_SEARCH)
    }
}
