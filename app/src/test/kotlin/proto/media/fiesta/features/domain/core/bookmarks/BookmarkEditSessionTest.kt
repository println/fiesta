package proto.media.fiesta.features.domain.core.bookmarks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BookmarkEditSessionTest {

    private val google = BookmarkEntry(0, "Google", "https://www.google.com", locked = false)
    private val netflix = BookmarkEntry(1, "Netflix", "https://www.netflix.com", locked = false)
    private val youtube = BookmarkEntry(2, "YouTube", "https://youtube.com", locked = true)

    private fun session() = BookmarkEditSession(listOf(google, netflix, youtube))

    private fun keys(session: BookmarkEditSession) = session.entries().map { it.key }

    @Test
    fun startsWithoutChanges() {
        assertFalse(session().hasChanges())
    }

    @Test
    fun moveDownSwapsWithNext() {
        val session = session()
        session.moveDown(0)
        assertEquals(listOf(1, 0, 2), keys(session))
        assertTrue(session.hasChanges())
    }

    @Test
    fun moveUpSwapsWithPrevious() {
        val session = session()
        session.moveUp(2)
        assertEquals(listOf(0, 2, 1), keys(session))
    }

    @Test
    fun firstCannotMoveUpAndLastCannotMoveDown() {
        val session = session()
        assertFalse(session.canMoveUp(0))
        assertFalse(session.canMoveDown(2))
        session.moveUp(0)
        session.moveDown(2)
        assertEquals(listOf(0, 1, 2), keys(session))
    }

    @Test
    fun editReplacesTitleAndUrl() {
        val session = session()
        session.edit(1, "Series", "https://netflix.com/browse")
        assertEquals(BookmarkEntry(1, "Series", "https://netflix.com/browse", locked = false), session.entries()[1])
    }

    @Test
    fun removeDropsEntryAndReportsKey() {
        val session = session()
        assertTrue(session.remove(1))
        assertEquals(listOf(0, 2), keys(session))
        assertEquals(setOf(1), session.removedKeys())
    }

    @Test
    fun lockedEntryIsNotRemoved() {
        val session = session()
        assertFalse(session.remove(2))
        assertEquals(emptySet<Int>(), session.removedKeys())
    }

    @Test
    fun movingBackRestoresNoChanges() {
        val session = session()
        session.moveDown(0)
        session.moveUp(0)
        assertFalse(session.hasChanges())
    }
}
