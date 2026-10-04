package proto.media.fiezta.features.domain.core.browser
import android.webkit.CookieManager

object CookieUtils {

    @JvmStatic
    fun flush() {
        CookieManager.getInstance().flush()
    }
}
