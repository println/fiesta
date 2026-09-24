package proto.media.fiesta.features.domain.core.bookmarks

import java.text.Normalizer

object BookmarkSearch {

    private val COMBINING_MARKS = Regex("\\p{Mn}+")

    fun matches(title: String?, url: String?, query: String): Boolean {
        val needle = normalize(query.trim())
        if (needle.isEmpty()) return true
        return listOfNotNull(title, url).any { normalize(it).contains(needle) }
    }

    private fun normalize(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD).replace(COMBINING_MARKS, "").lowercase()
}
