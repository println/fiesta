package proto.media.fiesta.features.domain.core.update

class AppVersion private constructor(private val parts: List<Int>) : Comparable<AppVersion> {

    override fun compareTo(other: AppVersion): Int {
        val length = maxOf(parts.size, other.parts.size)
        for (i in 0 until length) {
            val difference = parts.getOrElse(i) { 0 } - other.parts.getOrElse(i) { 0 }
            if (difference != 0) return difference
        }
        return 0
    }

    override fun toString(): String = parts.joinToString(".")

    companion object {
        fun parse(text: String): AppVersion? {
            val numbers = text.trim().removePrefix("v").removePrefix("V").split('.')
                .map { part -> part.toIntOrNull()?.takeIf { it >= 0 } ?: return null }
            return AppVersion(numbers)
        }
    }
}
