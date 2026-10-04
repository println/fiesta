package proto.media.fiezta.features.domain.core.bookmarks

interface BookmarksClickCallback {
    fun onBookmarkSelected(bookmark: Bookmark)
    fun onBookmarkFragmentClose()
}
