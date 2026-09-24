package proto.media.fiesta.support.webviewex.navigation

import java.net.URI
import java.net.URISyntaxException

object HostName {

    fun of(url: String?): String? {
        if (url == null) return null
        return try {
            URI(url).host?.removePrefix("www.")
        } catch (e: URISyntaxException) {
            null
        }
    }
}
