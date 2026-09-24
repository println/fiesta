package proto.media.fiesta.features.domain.core.tweaks

class TweakChanges {

    private class Entry(val initial: Boolean, var current: Boolean)

    private val entries = mutableMapOf<String, Entry>()

    fun recordInitial(key: String, value: Boolean) {
        entries.getOrPut(key) { Entry(value, value) }
    }

    fun update(key: String, value: Boolean) {
        entries.getOrPut(key) { Entry(value, value) }.current = value
    }

    fun shouldReload(): Boolean = entries.values.any { it.initial != it.current }
}
