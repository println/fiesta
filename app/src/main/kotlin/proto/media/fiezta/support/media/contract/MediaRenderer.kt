package proto.media.fiezta.support.media.contract

import proto.media.fiezta.support.media.dto.MediaCommandDto

interface MediaRenderer {
    val id: String
    fun execute(command: MediaCommandDto)
}
