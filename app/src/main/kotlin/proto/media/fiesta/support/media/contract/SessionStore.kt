package proto.media.fiesta.support.media.contract

import proto.media.fiesta.support.media.dto.MediaSnapshotDto

interface SessionStore {
    fun load(rendererId: String): MediaSnapshotDto?
    fun save(rendererId: String, snapshot: MediaSnapshotDto)
    fun clear(rendererId: String)
}
