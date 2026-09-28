package proto.media.fiesta.support.plugins.activation

object InstalledOptionKey {

    fun of(pluginId: String, script: String): String {
        val withoutExtension = script.removeSuffix(".js")
        val normalized = withoutExtension.replace('.', '_').replace('-', '_')
        return "pref_plugin_${pluginId}_$normalized"
    }
}
