package proto.media.fiesta.support.plugins.voice

import proto.media.fiesta.support.plugins.manifest.PluginDomain
import proto.media.fiesta.support.plugins.model.PluginManifest
import proto.media.fiesta.support.search.SearchEngineSelector

data class VoiceLanding(val url: String, val pluginId: String?, val script: String?)

class VoiceDestination(private val engines: SearchEngineSelector) {

    fun of(stack: List<PluginManifest>, selectedPluginId: String?, query: String, contextUrl: String?): VoiceLanding {
        val listener = VoiceListener.selected(stack, selectedPluginId)
        val resourceUrl = pluginResource(listener, query)
        if (resourceUrl != null && listener != null) {
            return VoiceLanding(resourceUrl, listener.id, listener.voice?.script)
        }
        return VoiceLanding(searchEngine(listener, contextUrl).searchUrl(query), null, null)
    }

    private fun pluginResource(listener: PluginManifest?, query: String): String? {
        val resource = listener?.voice?.resource ?: return null
        val domain = PluginDomain.of(listener) ?: return null
        return VoiceSearchUrl.of(domain, resource, query)
    }

    private fun searchEngine(listener: PluginManifest?, contextUrl: String?) =
        listener?.voice?.searchEngineId
            ?.let { id -> engines.engines.firstOrNull { it.id == id } }
            ?: engines.forUrl(contextUrl)
}
