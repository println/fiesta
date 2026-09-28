package proto.media.fiesta.support.webviewex.media

object MediaSessionCommands {

    fun play(): String = call("play")

    fun pause(): String = call("pause")

    fun seekTo(seconds: Int): String = call("seekTo", clampSeconds(seconds))

    fun seekBy(seconds: Int): String = call("seekBy", seconds)

    fun markInterrupted(): String = call("markInterrupted")

    fun resumeIfInterrupted(): String = call("resumeIfInterrupted")

    fun observe(): String = call("observe")

    internal fun clampSeconds(seconds: Int): Int = seconds.coerceAtLeast(0)

    private fun call(method: String, argument: Int? = null): String =
        "window.__webviewexMedia && window.__webviewexMedia.$method(${argument ?: ""});"
}
