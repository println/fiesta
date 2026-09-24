package proto.media.fiesta.features.domain.core.browser
import android.content.Context

object AutocompleteUtils {

    private val BUILT_IN_DOMAINS = arrayOf(
        "youtube.com",
        "m.youtube.com",
        "music.youtube.com",
        "google.com",
        "gmail.com",
        "github.com",
        "wikipedia.org",
        "reddit.com",
        "twitter.com",
        "x.com",
        "facebook.com",
        "instagram.com",
        "netflix.com",
        "amazon.com",
        "twitch.tv",
        "spotify.com",
    )

    @JvmStatic
    fun findMatch(context: Context, typed: String?): String? {
        if (typed.isNullOrEmpty()) {
            return null
        }
        for (domain in BUILT_IN_DOMAINS) {
            if (domain.startsWith(typed)) {
                return domain
            }
        }
        val visited = LinkedHashSet(BrowserStorageUtils.getRecordedHosts(context))
        for (host in visited) {
            if (host.lowercase().startsWith(typed)) {
                return host
            }
        }
        return null
    }
}
