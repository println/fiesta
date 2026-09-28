package proto.media.fiesta.support.plugins.manifest

import proto.media.fiesta.support.plugins.model.PluginManifest
import okhttp3.HttpUrl

object PluginDomain {

    fun of(manifest: PluginManifest): String? {
        val domains = manifest.rules
            .flatMap { it.match }
            .mapNotNull(::domainOf)
            .distinct()
        return domains.singleOrNull()
    }

    private fun domainOf(pattern: String): String? {
        val withoutScheme = pattern.substringAfter("://", missingDelimiterValue = "")
        if (withoutScheme.isEmpty()) return null
        val host = withoutScheme.substringBefore('/').removePrefix("*.")
        if (host.isEmpty() || '*' in host) return null
        return registrableDomain(host)
    }

    private fun registrableDomain(host: String): String? = try {
        HttpUrl.Builder().scheme("https").host(host).build().topPrivateDomain()
    } catch (e: IllegalArgumentException) {
        null
    }
}
