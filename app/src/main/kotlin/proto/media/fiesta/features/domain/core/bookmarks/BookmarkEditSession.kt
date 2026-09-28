package proto.media.fiesta.features.domain.core.bookmarks

data class BookmarkEntry(val key: Int, val title: String?, val url: String?, val locked: Boolean)

class BookmarkEditSession(initial: List<BookmarkEntry>) {

    private val initialEntries = initial
    private val entries = initial.toMutableList()

    fun entries(): List<BookmarkEntry> = entries.toList()

    fun canMoveUp(key: Int): Boolean = indexOf(key) > 0

    fun canMoveDown(key: Int): Boolean = indexOf(key).let { it >= 0 && it < entries.lastIndex }

    fun moveUp(key: Int) {
        if (canMoveUp(key)) swap(indexOf(key), indexOf(key) - 1)
    }

    fun moveDown(key: Int) {
        if (canMoveDown(key)) swap(indexOf(key), indexOf(key) + 1)
    }

    fun edit(key: Int, title: String, url: String) {
        val index = indexOf(key)
        if (index < 0) return
        entries[index] = entries[index].copy(title = title, url = url)
    }

    fun remove(key: Int): Boolean {
        val index = indexOf(key)
        if (index < 0 || entries[index].locked) return false
        entries.removeAt(index)
        return true
    }

    fun removedKeys(): Set<Int> = initialEntries.map { it.key }.toSet() - entries.map { it.key }.toSet()

    fun hasChanges(): Boolean = entries != initialEntries

    private fun indexOf(key: Int): Int = entries.indexOfFirst { it.key == key }

    private fun swap(a: Int, b: Int) {
        val moved = entries[a]
        entries[a] = entries[b]
        entries[b] = moved
    }
}
