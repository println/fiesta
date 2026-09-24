package proto.media.fiesta.support.search

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchEngineSelectorTest {

    private val google = testEngine("google")
    private val selector = SearchEngineSelector(
        engines = listOf(testEngine("youtube"), google),
        fallback = google
    )

    @Test
    fun youtubeWatchPageUsesYoutube() {
        assertEquals("youtube", selector.forUrl("https://www.youtube.com/watch?v=x").id)
    }

    @Test
    fun youtubeMobileSubdomainUsesYoutube() {
        assertEquals("youtube", selector.forUrl("https://m.youtube.com").id)
    }

    @Test
    fun youtuBeShortLinkUsesYoutube() {
        assertEquals("youtube", selector.forUrl("https://youtu.be/x").id)
    }

    @Test
    fun googleUsesGoogle() {
        assertEquals("google", selector.forUrl("https://www.google.com").id)
    }

    @Test
    fun anyOtherSiteFallsBackToGoogle() {
        assertEquals("google", selector.forUrl("https://example.com").id)
    }

    @Test
    fun nullUrlFallsBackToGoogle() {
        assertEquals("google", selector.forUrl(null).id)
    }

    @Test
    fun aboutBlankFallsBackToGoogle() {
        assertEquals("google", selector.forUrl("about:blank").id)
    }

    @Test
    fun youtubeUrlWithCharactersInvalidForUriUsesYoutube() {
        assertEquals("youtube", selector.forUrl("https://www.youtube.com/results?search_query=a b|c").id)
    }

    @Test
    fun similarLookingHostFallsBackToGoogle() {
        assertEquals("google", selector.forUrl("https://notyoutube.com").id)
    }
}
