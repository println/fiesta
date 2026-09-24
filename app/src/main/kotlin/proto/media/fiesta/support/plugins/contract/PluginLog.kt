package proto.media.fiesta.support.plugins.contract

interface PluginLog {

    fun debug(message: String)

    fun warn(message: String, cause: Throwable? = null)

    companion object {
        val SILENT = object : PluginLog {
            override fun debug(message: String) = Unit
            override fun warn(message: String, cause: Throwable?) = Unit
        }
    }
}
