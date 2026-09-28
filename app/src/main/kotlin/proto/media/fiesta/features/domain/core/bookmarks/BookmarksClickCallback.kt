package proto.media.fiesta.features.domain.core.bookmarks

interface BookmarksClickCallback {
    fun onBookmarkSelected(bookmark: Bookmark)
    fun onBookmarkFragmentClose()
}
