package proto.media.fiesta.support.plugins.voice

import proto.media.fiesta.support.plugins.model.PluginManifest
import proto.media.fiesta.support.plugins.model.PluginRule
import proto.media.fiesta.support.plugins.model.VoiceSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VoiceListenerTest {

    private fun manifest(id: String, voice: VoiceSpec?, fallback: Boolean = false) = PluginManifest(
        id = id,
        name = id,
        version = 1,
        rules = listOf(PluginRule(match = listOf("*"), events = "$id.events.js", tweaks = emptyList())),
        voice = voice,
        fallback = fallback
    )

    private val youtube = manifest("youtube", VoiceSpec(resource = "/results?search_query={searchTerms}", script = "voice-search.js", searchEngineId = null))
    private val generic = manifest("generic", VoiceSpec(resource = null, script = null, searchEngineId = "google"))
    private val fallbackGeneric = manifest("generic", VoiceSpec(resource = null, script = null, searchEngineId = "google"), fallback = true)
    private val gemini = manifest("gemini", VoiceSpec(resource = "/search?q={searchTerms}", script = null, searchEngineId = null))
    private val silent = manifest("silent", null)

    @Test
    fun onlyPluginsDeclaringVoiceAreCandidates() {
        assertEquals(listOf(youtube, generic), VoiceListener.candidates(listOf(youtube, silent, generic)))
    }

    @Test
    fun selectsTheChosenCandidate() {
        assertEquals(youtube, VoiceListener.selected(listOf(youtube, generic), "youtube"))
    }

    @Test
    fun fallsBackToTheLastCandidateWithoutAChoice() {
        assertEquals(generic, VoiceListener.selected(listOf(youtube, generic), null))
    }

    @Test
    fun fallsBackWhenTheChosenPluginLeftTheStack() {
        assertEquals(generic, VoiceListener.selected(listOf(silent, generic), "youtube"))
    }

    @Test
    fun prefersTheLastRegularCandidateOverTheFallback() {
        assertEquals(gemini, VoiceListener.selected(listOf(gemini, fallbackGeneric), null))
    }

    @Test
    fun replacesTheFallbackChoiceWithANewlyInstalledCandidate() {
        assertEquals(gemini, VoiceListener.selected(listOf(gemini, fallbackGeneric), "generic"))
    }

    @Test
    fun keepsTheFallbackWhenItIsTheOnlyCandidate() {
        assertEquals(fallbackGeneric, VoiceListener.selected(listOf(silent, fallbackGeneric), null))
    }

    @Test
    fun selectsNothingWhenNoPluginDeclaresVoice() {
        assertNull(VoiceListener.selected(listOf(silent), "silent"))
    }
}
