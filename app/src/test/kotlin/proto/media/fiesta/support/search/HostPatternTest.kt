package proto.media.fiesta.support.search

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HostPatternTest {

    @Test
    fun exactHostMatchesItself() {
        assertTrue(HostPattern.matches("google.com", "google.com"))
    }

    @Test
    fun exactHostIgnoresCase() {
        assertTrue(HostPattern.matches("google.com", "GOOGLE.com"))
    }

    @Test
    fun wildcardMatchesSubdomain() {
        assertTrue(HostPattern.matches("*.google.com", "www.google.com"))
    }

    @Test
    fun wildcardDoesNotMatchBareSuffix() {
        assertFalse(HostPattern.matches("*.google.com", "google.com"))
    }

    @Test
    fun wildcardDoesNotMatchSimilarLookingHost() {
        assertFalse(HostPattern.matches("*.google.com", "notgoogle.com"))
    }

    @Test
    fun acceptsExactAndWildcardForms() {
        assertTrue(HostPattern.isValid("youtu.be"))
        assertTrue(HostPattern.isValid("*.youtube.com"))
    }

    @Test
    fun rejectsBareWildcard() {
        assertFalse(HostPattern.isValid("*"))
    }

    @Test
    fun rejectsWildcardWithoutDot() {
        assertFalse(HostPattern.isValid("*google.com"))
    }

    @Test
    fun rejectsWildcardInTheMiddle() {
        assertFalse(HostPattern.isValid("a.*.com"))
    }

    @Test
    fun rejectsEmpty() {
        assertFalse(HostPattern.isValid(""))
    }
}
