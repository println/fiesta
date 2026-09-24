package proto.media.fiesta.support.media.server

import proto.media.fiesta.support.media.dto.MediaCommandDto
import proto.media.fiesta.support.media.dto.MediaReadingDto

internal data class CommandEcho(val command: MediaCommandDto, val expiresAtMillis: Long) {

    val expectsPlaying: Boolean
        get() = command == MediaCommandDto.Play

    fun survives(reading: MediaReadingDto, nowMillis: Long): Boolean {
        if (reading.playing == expectsPlaying) return false
        return nowMillis < expiresAtMillis
    }

    companion object {
        fun of(command: MediaCommandDto, expiresAtMillis: Long): CommandEcho? =
            if (command == MediaCommandDto.Play || command == MediaCommandDto.Pause) {
                CommandEcho(command, expiresAtMillis)
            } else {
                null
            }
    }
}
