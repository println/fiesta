package proto.media.fiesta.features.domain.core.browser
import android.net.Uri
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream

object AdBlockUtils {

    private val BLOCKED_DOMAINS: Set<String> = setOf(
        "imasdk.googleapis.com",
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "google-analytics.com",
        "googletagmanager.com",
        "googletagservices.com",
        "adservice.google.com",
        "pagead2.googlesyndication.com",
        "amazon-adsystem.com",
        "connect.facebook.net",
        "scorecardresearch.com",
        "adnxs.com",
        "outbrain.com",
        "taboola.com",
        "criteo.com",
        "moatads.com",
        "adsafeprotected.com"
    )

    @JvmStatic
    fun isBlocked(url: String?): Boolean {
        if (url == null) {
            return false
        }
        val host = try {
            Uri.parse(url).host
        } catch (e: Exception) {
            return false
        } ?: return false
        for (blocked in BLOCKED_DOMAINS) {
            if (host == blocked || host.endsWith(".$blocked")) {
                return true
            }
        }
        return false
    }

    @JvmStatic
    fun blockedResponse(): WebResourceResponse {
        return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
    }
}
