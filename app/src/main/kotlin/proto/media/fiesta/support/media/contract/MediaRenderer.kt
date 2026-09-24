package proto.media.fiesta.support.media.contract

import proto.media.fiesta.support.media.dto.MediaCommandDto

interface MediaRenderer {
    val id: String
    fun execute(command: MediaCommandDto)
}
