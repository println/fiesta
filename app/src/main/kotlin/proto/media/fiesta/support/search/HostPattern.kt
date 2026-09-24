package proto.media.fiesta.support.search

object HostPattern {

    private const val WILDCARD_PREFIX = "*."
    private val HOST = Regex("^[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*$")

    fun matches(pattern: String, host: String): Boolean {
        if (pattern.startsWith(WILDCARD_PREFIX)) {
            val dottedSuffix = pattern.substring(1)
            return host.length > dottedSuffix.length && host.endsWith(dottedSuffix, ignoreCase = true)
        }
        return host.equals(pattern, ignoreCase = true)
    }

    fun isValid(pattern: String): Boolean =
        HOST.matches(pattern.removePrefix(WILDCARD_PREFIX))
}
