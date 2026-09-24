package proto.media.fiesta.support.plugins.injection

object JsString {

    fun quote(value: String): String = buildString(value.length + 2) {
        append('"')
        for (c in value) {
            when (c) {
                '"', '\\', '/' -> append('\\').append(c)
                '\t' -> append("\\t")
                '\b' -> append("\\b")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\u000C' -> append("\\f")
                else -> if (c.code <= 0x1F) append(String.format("\\u%04x", c.code)) else append(c)
            }
        }
        append('"')
    }
}
