package proto.media.fiesta.support.plugins.voice

import proto.media.fiesta.support.plugins.model.PluginManifest
import proto.media.fiesta.support.plugins.model.PluginRule
import proto.media.fiesta.support.plugins.model.VoiceSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import proto.media.fiesta.support.search.SearchEngine
import proto.media.fiesta.support.search.SearchEngineSelector

class VoiceDestinationTest {

    private class FakeEngine(override val id: String, private val host: String) : SearchEngine {
        override fun handles(host: String) = host.endsWith(this.host)
        override fun searchUrl(query: String) = "https://$host/search?q=$query"
        override fun suggestionsUrl(query: String): String? = null
    }

    private val google = FakeEngine("google", "google.com")
    private val youtubeEngine = FakeEngine("youtube", "youtube.com")
    private val destination = VoiceDestination(SearchEngineSelector(listOf(google, youtubeEngine), google))

    private fun manifest(voice: VoiceSpec?, match: String = "https://*.youtube.com/*") = PluginManifest(
        id = "youtube",
        name = "YouTube",
        version = 1,
        rules = listOf(PluginRule(listOf(match), "events.js", emptyList())),
        voice = voice
    )

    @Test
    fun pluginResourceIsJoinedToThePluginDomain() {
        val listener = manifest(VoiceSpec(resource = "/results?search_query={searchTerms}", script = "voice-search.js", searchEngineId = null))
        assertEquals(
            "https://youtube.com/results?search_query=daft+punk",
            destination.of(listOf(listener), selectedPluginId = null, query = "daft punk", contextUrl = null).url
        )
    }

    @Test
    fun theOpenPageDoesNotChangeTheDestination() {
        val listener = manifest(VoiceSpec(resource = "/results?search_query={searchTerms}", script = "voice-search.js", searchEngineId = null))
        assertEquals(
            destination.of(listOf(listener), selectedPluginId = null, query = "daft punk", contextUrl = null).url,
            destination.of(listOf(listener), selectedPluginId = null, query = "daft punk", contextUrl = "https://m.youtube.com/watch?v=1").url
        )
    }

    @Test
    fun pluginCanDelegateToASearchEngine() {
        val listener = manifest(VoiceSpec(resource = null, script = null, searchEngineId = "youtube"))
        assertEquals("https://youtube.com/search?q=x", destination.of(listOf(listener), selectedPluginId = null, query = "x", contextUrl = null).url)
    }

    @Test
    fun withoutAListenerTheEngineOfTheOpenPageAnswers() {
        assertEquals(
            "https://youtube.com/search?q=x",
            destination.of(emptyList(), selectedPluginId = null, query = "x", contextUrl = "https://m.youtube.com/watch?v=1").url
        )
    }

    @Test
    fun aPluginOfManyDomainsFallsBackToTheEngine() {
        val listener = manifest(
            VoiceSpec(resource = "/results?search_query={searchTerms}", script = "voice-search.js", searchEngineId = null),
            match = "https://*/*"
        )
        assertEquals("https://google.com/search?q=x", destination.of(listOf(listener), selectedPluginId = null, query = "x", contextUrl = null).url)
    }

    @Test
    fun theUserChoosesWhichPluginListens() {
        val youtube = manifest(VoiceSpec(resource = "/results?search_query={searchTerms}", script = "voice-search.js", searchEngineId = null))
        val other = manifest(VoiceSpec(resource = null, script = null, searchEngineId = "google"))
            .copy(id = "other", rules = listOf(PluginRule(listOf("https://*.other.com/*"), "events.js", emptyList())))
        assertEquals(
            "https://google.com/search?q=x",
            destination.of(listOf(youtube, other), selectedPluginId = "other", query = "x", contextUrl = null).url
        )
    }

    @Test
    fun theLandingCarriesThePluginScript() {
        val listener = manifest(
            VoiceSpec(resource = "/results?search_query={searchTerms}", script = "voice-search.js", searchEngineId = null)
        )
        val landing = destination.of(listOf(listener), selectedPluginId = null, query = "x", contextUrl = null)
        assertEquals("youtube", landing.pluginId)
        assertEquals("voice-search.js", landing.script)
    }

    @Test
    fun aSearchEngineLandingCarriesNoScript() {
        val listener = manifest(VoiceSpec(resource = null, script = null, searchEngineId = "youtube"))
        val landing = destination.of(listOf(listener), selectedPluginId = null, query = "x", contextUrl = null)
        assertNull(landing.pluginId)
        assertNull(landing.script)
    }
}
