package proto.media.fiesta.shared.media

import org.json.JSONObject
import proto.media.fiesta.support.media.dto.MediaQueueDto
import proto.media.fiesta.support.media.dto.QueueEntryDto
import proto.media.fiesta.support.media.dto.QueueShape

object MediaQueueJson {

    fun parse(json: String): MediaQueueDto = try {
        if (json == "null") {
            MediaQueueDto.EMPTY
        } else {
            val obj = JSONObject(json)
            val entries = obj.optJSONArray("entries")
            val parsedEntries = (0 until (entries?.length() ?: 0)).map { index ->
                val entry = entries!!.getJSONObject(index)
                QueueEntryDto(
                    id = index.toLong(),
                    title = entry.optString("title", ""),
                    subtitle = entry.optString("subtitle", ""),
                    iconUrl = entry.optString("artwork", "")
                )
            }
            MediaQueueDto(
                title = obj.optString("title", ""),
                entries = parsedEntries,
                cursor = obj.optInt("cursor", -1),
                shape = shapeOf(obj.optString("shape", ""))
            )
        }
    } catch (e: Exception) {
        MediaQueueDto.EMPTY
    }

    private fun shapeOf(shape: String) = when (shape) {
        "list" -> QueueShape.LIST
        "stream" -> QueueShape.STREAM
        else -> QueueShape.NONE
    }
}
