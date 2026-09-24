package proto.media.fiesta.support.webviewex.media

enum class MediaElementState { NONE, LOADING, PLAYING, PAUSED, ENDED, EMPTIED }

enum class MediaElementEvent(val jsName: String) {
    EMPTIED("emptied"),
    LOAD_START("loadstart"),
    LOADED_METADATA("loadedmetadata"),
    SEEKED("seeked"),
    WAITING("waiting"),
    PLAYING("playing"),
    PAUSE("pause"),
    ENDED("ended"),
    TIME_UPDATE("timeupdate");

    companion object {
        fun fromJs(name: String): MediaElementEvent? = values().firstOrNull { it.jsName == name }
    }
}

class MediaElementTracker {

    var state: MediaElementState = MediaElementState.NONE
        private set

    var trustedPositionSeconds: Double = 0.0
        private set

    fun onEvent(event: MediaElementEvent, positionSeconds: Double) {
        state = when (event) {
            MediaElementEvent.EMPTIED -> MediaElementState.EMPTIED
            MediaElementEvent.LOAD_START, MediaElementEvent.WAITING -> MediaElementState.LOADING
            MediaElementEvent.PLAYING -> MediaElementState.PLAYING
            MediaElementEvent.PAUSE -> if (state == MediaElementState.ENDED) state else MediaElementState.PAUSED
            MediaElementEvent.ENDED -> MediaElementState.ENDED
            MediaElementEvent.LOADED_METADATA, MediaElementEvent.SEEKED, MediaElementEvent.TIME_UPDATE -> state
        }
        if (carriesTrustedPosition(event)) trustedPositionSeconds = positionSeconds
    }

    fun reset() {
        state = MediaElementState.NONE
        trustedPositionSeconds = 0.0
    }

    private fun carriesTrustedPosition(event: MediaElementEvent): Boolean =
        state != MediaElementState.EMPTIED && state != MediaElementState.LOADING &&
            (event == MediaElementEvent.SEEKED || event == MediaElementEvent.TIME_UPDATE ||
                event == MediaElementEvent.PLAYING || event == MediaElementEvent.PAUSE)
}
