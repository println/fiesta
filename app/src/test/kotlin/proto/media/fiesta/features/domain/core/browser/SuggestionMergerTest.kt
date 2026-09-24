package proto.media.fiesta.features.domain.core.browser

import proto.media.fiesta.features.domain.core.browser.SuggestionMerger.Suggestion
import proto.media.fiesta.features.domain.core.browser.SuggestionMerger.Type
import org.junit.Assert.assertEquals
import org.junit.Test

class SuggestionMergerTest {

    @Test
    fun ordersInlineBeforeHistoryBeforeBookmarksBeforeSearch() {
        val inline = Suggestion(Type.INLINE_COMPLETION, "youtube.com", "https://youtube.com", "youtube.com")
        val history = Suggestion(Type.HISTORY, "github.com", "https://github.com", "github.com")
        val bookmark = Suggestion(Type.BOOKMARK, "Netflix", "https://netflix.com", "netflix.com")
        val search = Suggestion(Type.SEARCH, "cats", "https://www.google.com/search?q=cats", null)

        val merged = SuggestionMerger.merge(inline, listOf(history), listOf(bookmark), listOf(search), limit = 6)

        assertEquals(listOf(inline, history, bookmark, search), merged)
    }

    @Test
    fun dedupsByHostKeepingHigherPriorityType() {
        val inline = Suggestion(Type.INLINE_COMPLETION, "youtube.com", "https://youtube.com", "youtube.com")
        val history = Suggestion(Type.HISTORY, "youtube.com", "https://youtube.com/feed", "youtube.com")

        val merged = SuggestionMerger.merge(inline, listOf(history), emptyList(), emptyList(), limit = 6)

        assertEquals(listOf(inline), merged)
    }

    @Test
    fun limitsNumberOfRows() {
        val history = (1..10).map { Suggestion(Type.HISTORY, "site$it.com", "https://site$it.com", "site$it.com") }

        val merged = SuggestionMerger.merge(null, history, emptyList(), emptyList(), limit = 3)

        assertEquals(3, merged.size)
    }

    @Test
    fun searchSuggestionsWithoutHostAreNeverDeduped() {
        val search1 = Suggestion(Type.SEARCH, "cats", "https://www.google.com/search?q=cats", null)
        val search2 = Suggestion(Type.SEARCH, "cats video", "https://www.google.com/search?q=cats+video", null)

        val merged = SuggestionMerger.merge(null, emptyList(), emptyList(), listOf(search1, search2), limit = 6)

        assertEquals(listOf(search1, search2), merged)
    }
}
