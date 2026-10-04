package proto.media.fiezta.support.media.dto

data class MediaTrackDto(
    val identity: String,
    val title: String,
    val artist: String,
    val artworkUrl: String
) {
    val hasMetadata: Boolean
        get() = title.isNotEmpty()

    companion object {
        val EMPTY = MediaTrackDto(identity = "", title = "", artist = "", artworkUrl = "")
    }
}
