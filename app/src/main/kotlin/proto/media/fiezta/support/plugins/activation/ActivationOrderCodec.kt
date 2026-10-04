package proto.media.fiezta.support.plugins.activation

object ActivationOrderCodec {

    fun encode(ids: List<String>): String = ids.joinToString(",")

    fun decode(text: String?): List<String> =
        text?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
}
