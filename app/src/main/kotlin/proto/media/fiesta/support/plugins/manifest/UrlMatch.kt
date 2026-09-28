package proto.media.fiesta.support.plugins.manifest

object UrlMatch {

    private const val UNIVERSAL_PROBE_A = "https://a.example/"
    private const val UNIVERSAL_PROBE_B = "http://b.test/x?y"

    fun matches(pattern: String, url: String): Boolean {
        val regex = pattern.split("*").joinToString(".*") { Regex.escape(it) }
        return Regex(regex, RegexOption.IGNORE_CASE).matches(url)
    }

    fun isUniversal(pattern: String): Boolean =
        matches(pattern, UNIVERSAL_PROBE_A) && matches(pattern, UNIVERSAL_PROBE_B)
}
