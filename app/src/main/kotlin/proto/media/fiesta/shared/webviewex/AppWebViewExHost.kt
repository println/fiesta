package proto.media.fiesta.shared.webviewex

import android.content.Context
import proto.media.fiesta.BuildConfig
import proto.media.fiesta.features.domain.core.browser.AdBlockUtils
import proto.media.fiesta.features.domain.core.settings.SettingsStorage
import proto.media.fiesta.features.domain.core.settings.SettingsUtils
import proto.media.fiesta.support.webviewex.WebViewExHost

class AppWebViewExHost(
    private val context: Context,
    private val onBlocked: (String) -> Unit = {}
) : WebViewExHost {

    override fun prefersDesktop(host: String?): Boolean = SettingsStorage.isDesktopEnabledForHost(context, host)

    override fun stripWebViewMarker(): Boolean = SettingsUtils.isStripWebViewUserAgent(context)

    override fun thirdPartyCookiesAllowed(): Boolean = SettingsUtils.isThirdPartyCookiesAllowed(context)

    override fun isDebuggable(): Boolean = BuildConfig.DEBUG

    override fun isRequestBlocked(url: String, pageHost: String?): Boolean =
        SettingsStorage.isAdBlockEnabledForHost(context, pageHost) && AdBlockUtils.isBlocked(url)

    override fun onRequestBlocked(url: String) = onBlocked(url)
}
