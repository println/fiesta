package proto.media.fiesta.features.domain.core.bookmarks

import java.net.URI
import java.util.Locale

object BookmarkMonogram {
    private val TITLE_SITE_SEPARATOR = Regex("\\s+[-–—|·•]\\s+")
    private val BRACKETED = Regex("\\([^)]*\\)|\\[[^\\]]*\\]|\\{[^\\}]*\\}")
    private val HOST_PREFIXES = listOf("www.", "m.", "mobile.")

    @JvmStatic
    fun of(title: String?, url: String?): String {
        val fromTitle = initialsOf(wordsOf(siteNameOf(title.orEmpty())))
        return if (fromTitle.isNotEmpty()) fromTitle else initialsOf(wordsOf(hostLabelOf(url)))
    }

    @JvmStatic
    fun paletteIndex(monogram: String, paletteSize: Int): Int = Math.floorMod(monogram.hashCode(), paletteSize)

    private fun siteNameOf(title: String): String {
        val withoutBrackets = BRACKETED.replace(title, " ")
        return withoutBrackets.split(TITLE_SITE_SEPARATOR).firstOrNull { wordsOf(it).isNotEmpty() }.orEmpty()
    }

    private fun wordsOf(text: String): List<String> {
        val words = mutableListOf<String>()
        val current = StringBuilder()
        var offset = 0
        while (offset < text.length) {
            val codePoint = text.codePointAt(offset)
            if (Character.isLetterOrDigit(codePoint)) {
                current.appendCodePoint(codePoint)
            } else if (current.isNotEmpty()) {
                words.add(current.toString())
                current.setLength(0)
            }
            offset += Character.charCount(codePoint)
        }
        if (current.isNotEmpty()) words.add(current.toString())
        return words
    }

    private fun initialsOf(words: List<String>): String = when {
        words.isEmpty() -> ""
        words.size == 1 -> {
            val word = words[0]
            val first = firstCodePoint(word)
            first.uppercase(Locale.ROOT) + firstCodePoint(word.substring(first.length)).lowercase(Locale.ROOT)
        }
        else -> firstCodePoint(words.first()).uppercase(Locale.ROOT) + firstCodePoint(words.last()).uppercase(Locale.ROOT)
    }

    private fun firstCodePoint(text: String): String =
        if (text.isEmpty()) "" else text.substring(0, text.offsetByCodePoints(0, 1))

    private fun hostLabelOf(url: String?): String {
        if (url.isNullOrEmpty()) return ""
        val host = try {
            URI(url).host
        } catch (e: Exception) {
            null
        } ?: return ""
        val withoutPrefix = HOST_PREFIXES.fold(host.lowercase(Locale.ROOT)) { acc, prefix -> acc.removePrefix(prefix) }
        return withoutPrefix.substringBefore('.')
    }
}
