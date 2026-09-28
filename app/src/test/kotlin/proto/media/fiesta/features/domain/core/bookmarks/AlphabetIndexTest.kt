package proto.media.fiesta.features.domain.core.bookmarks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AlphabetIndexTest {

    @Test
    fun accentedUppercaseTitleGoesUnderBaseLetter() {
        assertEquals('A', AlphabetIndex.sectionKeyOf("Ângela", "https://example.com"))
    }

    @Test
    fun accentedLowercaseTitleGoesUnderUppercaseBaseLetter() {
        assertEquals('A', AlphabetIndex.sectionKeyOf("ábaco", "https://example.com"))
    }

    @Test
    fun nullTitleFallsBackToUrlHost() {
        assertEquals('E', AlphabetIndex.sectionKeyOf(null, "https://example.com"))
    }

    @Test
    fun symbolTitleGoesToOtherSection() {
        assertEquals('#', AlphabetIndex.sectionKeyOf("42 Reasons", "https://example.com"))
    }

    @Test
    fun blankTitleAndUrlGoesToOtherSection() {
        assertEquals('#', AlphabetIndex.sectionKeyOf(null, null))
    }

    @Test
    fun positionOfFindsFirstOccurrence() {
        val keys = listOf('#', 'A', 'A', 'B')
        assertEquals(1, AlphabetIndex.positionOf('A', keys))
        assertEquals(0, AlphabetIndex.positionOf('#', keys))
        assertNull(AlphabetIndex.positionOf('Z', keys))
    }

    @Test
    fun availableLettersReturnsUniqueSet() {
        val keys = listOf('#', 'A', 'A', 'B')
        assertEquals(setOf('#', 'A', 'B'), AlphabetIndex.availableLetters(keys))
    }
}
