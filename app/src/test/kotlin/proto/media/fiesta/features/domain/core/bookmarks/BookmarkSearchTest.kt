package proto.media.fiesta.features.domain.core.bookmarks

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BookmarkSearchTest {

    @Test
    fun blankQueryMatchesEverything() {
        assertTrue(BookmarkSearch.matches("YouTube", "https://youtube.com", "  "))
    }

    @Test
    fun matchesTitleIgnoringCase() {
        assertTrue(BookmarkSearch.matches("YouTube", "https://youtube.com", "tube"))
    }

    @Test
    fun matchesUrl() {
        assertTrue(BookmarkSearch.matches("Streaming", "https://www.netflix.com", "netflix"))
    }

    @Test
    fun ignoresAccents() {
        assertTrue(BookmarkSearch.matches("Música", null, "musica"))
    }

    @Test
    fun rejectsWhenNeitherTitleNorUrlContainQuery() {
        assertFalse(BookmarkSearch.matches("Google", "https://www.google.com", "netflix"))
    }

    @Test
    fun bookmarkWithoutTitleAndUrlOnlyMatchesBlankQuery() {
        assertFalse(BookmarkSearch.matches(null, null, "a"))
    }
}
