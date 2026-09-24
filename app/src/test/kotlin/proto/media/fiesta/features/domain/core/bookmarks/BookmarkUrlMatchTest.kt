package proto.media.fiesta.features.domain.core.bookmarks

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BookmarkUrlMatchTest {

    @Test
    fun ignoresFragment() {
        assertTrue(BookmarkUrlMatch.sameUrl("https://youtube.com/watch?v=1#t=10", "https://youtube.com/watch?v=1"))
    }

    @Test
    fun ignoresTrailingSlash() {
        assertTrue(BookmarkUrlMatch.sameUrl("https://youtube.com/", "https://youtube.com"))
    }

    @Test
    fun ignoresHttpVsHttps() {
        assertTrue(BookmarkUrlMatch.sameUrl("http://youtube.com", "https://youtube.com"))
    }

    @Test
    fun ignoresMobileSubdomain() {
        assertTrue(BookmarkUrlMatch.sameUrl("https://m.youtube.com/watch?v=1", "https://www.youtube.com/watch?v=1"))
    }

    @Test
    fun differentPathsDoNotMatch() {
        assertFalse(BookmarkUrlMatch.sameUrl("https://youtube.com/watch?v=1", "https://youtube.com/watch?v=2"))
    }

    @Test
    fun nullsAreEqualOnlyToEachOther() {
        assertTrue(BookmarkUrlMatch.sameUrl(null, null))
        assertFalse(BookmarkUrlMatch.sameUrl(null, "https://youtube.com"))
    }
}
