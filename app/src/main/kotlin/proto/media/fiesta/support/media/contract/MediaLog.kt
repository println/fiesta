package proto.media.fiesta.support.media.contract

interface MediaLog {

    fun debug(message: String)

    fun error(message: String, cause: Throwable? = null)

    companion object {
        val SILENT = object : MediaLog {
            override fun debug(message: String) = Unit
            override fun error(message: String, cause: Throwable?) = Unit
        }
    }
}
