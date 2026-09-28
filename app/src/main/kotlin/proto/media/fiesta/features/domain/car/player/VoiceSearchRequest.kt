package proto.media.fiesta.features.domain.car.player

class VoiceSearchRequest private constructor(
    val id: Long,
    val query: String,
    val deadlineAtMillis: Long,
    val resultDeadlineAtMillis: Long,
    val documentGeneration: Long,
    val stage: Stage
) {
    enum class Stage {
        WAITING_FOR_PLUGIN,
        SEARCHING,
        OPENING_RESULT,
        WAITING_FOR_PLAYBACK
    }

    fun belongsTo(id: Long, generation: Long) = this.id == id && documentGeneration == generation

    fun hasExpired(nowMillis: Long) = nowMillis >= deadlineAtMillis

    fun withDocument(generation: Long) = VoiceSearchRequest(
        id, query, deadlineAtMillis, resultDeadlineAtMillis, generation, Stage.WAITING_FOR_PLUGIN
    )

    fun advance(stage: Stage) = VoiceSearchRequest(
        id, query, deadlineAtMillis, resultDeadlineAtMillis, documentGeneration, stage
    )

    companion object {
        const val TOTAL_TIMEOUT_MILLIS = 30_000L
        const val RESULT_TIMEOUT_MILLIS = 10_000L

        fun create(id: Long, query: String, nowMillis: Long) = VoiceSearchRequest(
            id = id,
            query = query,
            deadlineAtMillis = nowMillis + TOTAL_TIMEOUT_MILLIS,
            resultDeadlineAtMillis = nowMillis + RESULT_TIMEOUT_MILLIS,
            documentGeneration = 0,
            stage = Stage.WAITING_FOR_PLUGIN
        )
    }
}
