package proto.media.fiesta.features.domain.core.bookmarks

import java.net.URI
import java.text.Normalizer
import java.text.Collator
import java.util.Locale

object AlphabetIndex {

    const val OTHER_SECTION = '#'

    private val collator: Collator = Collator.getInstance(Locale.forLanguageTag("pt-BR"))

    fun sectionKeyOf(title: String?, url: String?): Char {
        val source = title?.takeIf { it.isNotBlank() } ?: hostOf(url)
        val firstChar = source?.let(::stripAccents)?.trim()?.firstOrNull()?.uppercaseChar()
        return if (firstChar != null && firstChar in 'A'..'Z') firstChar else OTHER_SECTION
    }

    fun compare(aTitle: String?, aUrl: String?, bTitle: String?, bUrl: String?): Int {
        val keyA = sectionKeyOf(aTitle, aUrl)
        val keyB = sectionKeyOf(bTitle, bUrl)
        return when {
            keyA == OTHER_SECTION && keyB != OTHER_SECTION -> -1
            keyB == OTHER_SECTION && keyA != OTHER_SECTION -> 1
            else -> collator.compare(sortLabel(aTitle, aUrl), sortLabel(bTitle, bUrl))
        }
    }

    fun positionOf(letter: Char, keysInListOrder: List<Char>): Int? =
        keysInListOrder.indexOf(letter).takeIf { it >= 0 }

    fun availableLetters(keysInListOrder: List<Char>): Set<Char> = keysInListOrder.toSet()

    private fun sortLabel(title: String?, url: String?): String =
        title?.takeIf { it.isNotBlank() } ?: url.orEmpty()

    private fun hostOf(url: String?): String? {
        if (url.isNullOrBlank()) {
            return null
        }
        return try {
            URI(url).host
        } catch (e: Exception) {
            null
        }
    }

    private fun stripAccents(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")
}
