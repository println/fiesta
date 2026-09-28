package proto.media.fiesta.support.plugins.voice

import proto.media.fiesta.support.plugins.model.PluginManifest

object VoiceListener {

    fun candidates(stack: List<PluginManifest>): List<PluginManifest> =
        stack.filter { it.voice != null }

    fun selected(stack: List<PluginManifest>, selectedId: String?): PluginManifest? {
        val candidates = candidates(stack)
        val chosen = candidates.firstOrNull { it.id == selectedId }
        if (chosen != null && !chosen.fallback) return chosen
        return candidates.lastOrNull { !it.fallback } ?: candidates.lastOrNull()
    }
}
