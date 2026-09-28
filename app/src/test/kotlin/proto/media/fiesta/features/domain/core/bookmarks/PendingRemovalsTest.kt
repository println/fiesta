package proto.media.fiesta.features.domain.core.bookmarks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PendingRemovalsTest {

    @Test
    fun emptyByDefault() {
        val removals = PendingRemovals()
        assertTrue(removals.toCommit().isEmpty())
    }

    @Test
    fun toggleMarksPending() {
        val removals = PendingRemovals()
        removals.toggle("1")
        assertTrue(removals.isPending("1"))
        assertEquals(listOf("1"), removals.toCommit())
    }

    @Test
    fun togglingTwiceUndoesIt() {
        val removals = PendingRemovals()
        removals.toggle("1")
        removals.toggle("1")
        assertFalse(removals.isPending("1"))
        assertTrue(removals.toCommit().isEmpty())
    }

    @Test
    fun tracksMultipleIndependently() {
        val removals = PendingRemovals()
        removals.toggle("1")
        removals.toggle("2")
        removals.toggle("1")
        assertEquals(listOf("2"), removals.toCommit())
    }
}
