package proto.media.fiesta.support.plugins.manifest

import proto.media.fiesta.support.plugins.model.PluginManifest
import proto.media.fiesta.support.plugins.model.PluginRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PluginDomainTest {

    private fun manifestOf(vararg match: String) = PluginManifest(
        id = "plugin",
        name = "Plugin",
        version = 1,
        rules = match.map { PluginRule(listOf(it), "events.js", emptyList()) }
    )

    @Test
    fun subdomainsOfTheSameSiteAreOneDomain() {
        val manifest = manifestOf("https://youtube.com/*", "https://*.youtube.com/watch?*")
        assertEquals("youtube.com", PluginDomain.of(manifest))
    }

    @Test
    fun twoSitesHaveNoSingleDomain() {
        assertNull(PluginDomain.of(manifestOf("https://youtube.com/*", "https://vimeo.com/*")))
    }

    @Test
    fun universalMatchHasNoDomain() {
        assertNull(PluginDomain.of(manifestOf("https://*/*")))
    }
}
