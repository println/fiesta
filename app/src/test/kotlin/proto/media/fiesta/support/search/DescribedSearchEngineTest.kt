package proto.media.fiesta.support.search

import org.junit.Assert.assertEquals
import org.junit.Test

class DescribedSearchEngineTest {

    private val google = testEngine("google")
    private val youtube = testEngine("youtube")

    @Test
    fun googleEncodesSpace() {
        assertEquals("https://www.google.com/search?q=a+b", google.searchUrl("a b"))
    }

    @Test
    fun googleEncodesAccent() {
        assertEquals("https://www.google.com/search?q=caf%C3%A9", google.searchUrl("café"))
    }

    @Test
    fun googleEncodesAmpersand() {
        assertEquals("https://www.google.com/search?q=rock+%26+roll", google.searchUrl("rock & roll"))
    }

    @Test
    fun googleSuggestionsUrl() {
        assertEquals(
            "https://suggestqueries.google.com/complete/search?client=chrome&q=a+b",
            google.suggestionsUrl("a b")
        )
    }

    @Test
    fun youtubeEncodesSpace() {
        assertEquals("https://www.youtube.com/results?search_query=a+b", youtube.searchUrl("a b"))
    }

    @Test
    fun youtubeEncodesAccent() {
        assertEquals("https://www.youtube.com/results?search_query=caf%C3%A9", youtube.searchUrl("café"))
    }

    @Test
    fun youtubeEncodesAmpersand() {
        assertEquals(
            "https://www.youtube.com/results?search_query=rock+%26+roll",
            youtube.searchUrl("rock & roll")
        )
    }

    @Test
    fun youtubeSuggestionsUrl() {
        assertEquals(
            "https://suggestqueries.google.com/complete/search?client=firefox&ds=yt&q=a+b",
            youtube.suggestionsUrl("a b")
        )
    }
}
