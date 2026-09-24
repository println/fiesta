package proto.media.fiesta.support.plugins.contract

interface PluginPageListener {
    fun onHandlersChanged(events: Set<String>)

    fun onVoiceSearchProgress(id: Long, generation: Long, stage: String) = Unit
}
