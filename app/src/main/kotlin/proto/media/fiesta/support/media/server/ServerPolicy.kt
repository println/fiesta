package proto.media.fiesta.support.media.server

import proto.media.fiesta.support.media.config.MediaDefaults

data class ServerPolicy(
    val commandTimeoutMillis: Long = MediaDefaults.COMMAND_TIMEOUT_MILLIS
) {
    companion object {
        val DEFAULT = ServerPolicy()
    }
}
