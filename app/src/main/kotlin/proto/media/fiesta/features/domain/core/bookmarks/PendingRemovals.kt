package proto.media.fiesta.features.domain.core.bookmarks

class PendingRemovals {

    private val pending = mutableSetOf<String>()

    fun toggle(id: String) {
        if (!pending.remove(id)) {
            pending.add(id)
        }
    }

    fun isPending(id: String): Boolean = pending.contains(id)

    fun toCommit(): List<String> = pending.toList()

    fun clear() {
        pending.clear()
    }
}
