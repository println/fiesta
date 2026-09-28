package proto.media.fiesta.support.plugins.manifest

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlMatchTest {

    @Test
    fun matchesLiteralPattern() {
        assertTrue(UrlMatch.matches("https://example.com/", "https://example.com/"))
        assertFalse(UrlMatch.matches("https://example.com/", "https://example.com/other"))
    }

    @Test
    fun wildcardInMiddleMatchesAnySequence() {
        assertTrue(UrlMatch.matches("https://*.youtube.com/*", "https://www.youtube.com/watch?v=1"))
    }

    @Test
    fun wildcardAtEndMatchesEmpty() {
        assertTrue(UrlMatch.matches("https://youtube.com/*", "https://youtube.com/"))
    }

    @Test
    fun wildcardMatchesEmptySequence() {
        assertTrue(UrlMatch.matches("https://youtube.com/*shorts", "https://youtube.com/shorts"))
    }

    @Test
    fun questionMarkIsLiteral() {
        assertTrue(UrlMatch.matches("https://youtube.com/watch?*", "https://youtube.com/watch?v=1"))
        assertFalse(UrlMatch.matches("https://youtube.com/watch?*", "https://youtube.com/watchXv=1"))
    }

    @Test
    fun matchingIsCaseInsensitive() {
        assertTrue(UrlMatch.matches("https://YouTube.com/*", "https://youtube.com/watch"))
    }

    @Test
    fun subdomainWildcardDoesNotMatchBareDomain() {
        assertFalse(UrlMatch.matches("https://*.youtube.com/*", "https://youtube.com/"))
    }

    @Test
    fun universalPatterns() {
        assertTrue(UrlMatch.isUniversal("*"))
        assertTrue(UrlMatch.isUniversal("*://*/*"))
        assertTrue(UrlMatch.isUniversal("http*"))
    }

    @Test
    fun nonUniversalPattern() {
        assertFalse(UrlMatch.isUniversal("https://*.youtube.com/*"))
    }
}
