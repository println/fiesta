package proto.media.fiesta.support.webviewex.gearhead

enum class AudioOutputState { ACTIVE, IDLE, FOCUS_LOST }

enum class ObservedCommandKind { PLAY, PAUSE, STOP }

data class ObservedCommand(val kind: ObservedCommandKind, val origin: String, val atMillis: Long)

interface AudioOutputSignal {
    fun observe(listener: (AudioOutputState) -> Unit): AutoCloseable
}

interface CarConnectionSignal {
    fun observe(listener: (connected: Boolean) -> Unit): AutoCloseable
}

interface CarModeExitSignal {
    fun observe(listener: () -> Unit): AutoCloseable
}
