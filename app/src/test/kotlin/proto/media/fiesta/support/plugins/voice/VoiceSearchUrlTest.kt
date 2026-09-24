package proto.media.fiesta.support.plugins.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceSearchUrlTest {

    @Test
    fun resourceMustStartWithSlash() {
        assertFalse(VoiceSearchUrl.isValidResource("results?search_query={searchTerms}"))
    }

    @Test
    fun resourceMustHoldTheSearchTerms() {
        assertFalse(VoiceSearchUrl.isValidResource("/results"))
    }

    @Test
    fun resourceRejectsAnotherPlaceholder() {
        assertFalse(VoiceSearchUrl.isValidResource("/results?q={searchTerms}&lang={locale}"))
    }

    @Test
    fun resourceOfTheYoutubePluginIsValid() {
        assertTrue(VoiceSearchUrl.isValidResource("/results?search_query={searchTerms}"))
    }

    @Test
    fun urlJoinsDomainAndResource() {
        assertEquals(
            "https://youtube.com/results?search_query=daft+punk",
            VoiceSearchUrl.of("youtube.com", "/results?search_query={searchTerms}", "daft punk")
        )
    }

    @Test
    fun urlEncodesTheQuery() {
        assertEquals(
            "https://youtube.com/results?search_query=a%26b",
            VoiceSearchUrl.of("youtube.com", "/results?search_query={searchTerms}", "a&b")
        )
    }
}
