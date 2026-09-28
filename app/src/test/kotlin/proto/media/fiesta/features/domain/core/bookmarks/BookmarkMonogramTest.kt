package proto.media.fiesta.features.domain.core.bookmarks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BookmarkMonogramTest {

    private fun of(title: String?, url: String? = "https://example.com") = BookmarkMonogram.of(title, url)

    @Test
    fun usesFirstAndLastWordInitials() {
        assertEquals("X1", of("Xpto 123 - domain"))
        assertEquals("GM", of("Google Play Music"))
    }

    @Test
    fun usesFirstTwoLettersOfSingleWordWithSecondLowercase() {
        assertEquals("Xp", of("Xpto - domain"))
        assertEquals("Yo", of("YOUTUBE"))
    }

    @Test
    fun dropsSiteSuffixAfterCommonTitleSeparators() {
        assertEquals("Yo", of("YouTube – youtube.com"))
        assertEquals("NF", of("Netflix Filmes | Netflix"))
        assertEquals("Sp", of("Spotify · Web Player"))
    }

    @Test
    fun keepsHyphenInsideWordsAsWordBoundary() {
        assertEquals("X1", of("Xpto-123"))
    }

    @Test
    fun ignoresBracketedContent() {
        assertEquals("Yo", of("(3) YouTube"))
        assertEquals("GM", of("Gmail [Beta] Mail"))
    }

    @Test
    fun ignoresSymbolsAndEmoji() {
        assertEquals("Pa", of("😀 Party!"))
        assertEquals("AB", of("  A & B  "))
    }

    @Test
    fun keepsAccentsAndNonLatinLetters() {
        assertEquals("Éc", of("École"))
        assertEquals("Пр", of("Привет"))
    }

    @Test
    fun uppercasesIndependentlyOfDeviceLocale() {
        assertEquals("In", of("inbox"))
    }

    @Test
    fun fallsBackToHostLabelWhenTitleHasNoLetters() {
        assertEquals("Yo", of("", "https://www.youtube.com/watch?v=1"))
        assertEquals("Pl", of("   ", "https://plex.tv"))
        assertEquals("Wi", of("🎵🎵", "https://m.wikipedia.org"))
        assertEquals("Go", of(null, "https://www.google.com"))
    }

    @Test
    fun returnsEmptyWhenNothingUsable() {
        assertEquals("", of("---", null))
        assertEquals("", of(null, "not a url"))
    }

    @Test
    fun paletteIndexIsStableAndWithinBounds() {
        val index = BookmarkMonogram.paletteIndex("X1", 10)
        assertEquals(index, BookmarkMonogram.paletteIndex("X1", 10))
        assertTrue(index in 0 until 10)
        assertTrue(BookmarkMonogram.paletteIndex("", 10) in 0 until 10)
    }
}
