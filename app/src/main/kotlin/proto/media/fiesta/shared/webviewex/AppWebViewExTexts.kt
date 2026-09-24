package proto.media.fiesta.shared.webviewex

import android.content.Context
import proto.media.fiesta.R
import proto.media.fiesta.support.webviewex.ErrorAction
import proto.media.fiesta.support.webviewex.WebViewExTexts
import proto.media.fiesta.support.webviewex.WebViewExTheme
import proto.media.fiesta.support.webviewex.navigation.NavigationFailure

class AppWebViewExTexts(private val context: Context) : WebViewExTexts {

    override fun title(failure: NavigationFailure): String = context.getString(R.string.error_page_title)

    override fun message(failure: NavigationFailure): String = when (failure) {
        NavigationFailure.Offline -> context.getString(R.string.error_msg_offline)
        NavigationFailure.DnsFailure -> context.getString(R.string.error_msg_dns)
        NavigationFailure.ConnectionRefused -> context.getString(R.string.error_msg_connection)
        NavigationFailure.Timeout -> context.getString(R.string.error_msg_timeout)
        NavigationFailure.SslError -> context.getString(R.string.error_page_ssl)
        NavigationFailure.CleartextBlocked -> context.getString(R.string.error_msg_cleartext)
        is NavigationFailure.HttpStatus -> context.getString(R.string.error_msg_http, failure.code)
        NavigationFailure.UnsupportedScheme -> context.getString(R.string.error_msg_scheme)
        NavigationFailure.AppNotInstalled -> context.getString(R.string.error_msg_app_missing)
        NavigationFailure.BlockedByContentFilter -> context.getString(R.string.error_msg_blocked)
        NavigationFailure.Unknown -> context.getString(R.string.error_msg_unknown)
    }

    override fun actionLabel(action: ErrorAction): String = context.getString(
        when (action) {
            ErrorAction.RETRY -> R.string.error_page_retry
            ErrorAction.RETRY_HTTP -> R.string.error_action_retry_http
            ErrorAction.GO_BACK -> R.string.error_action_back
            ErrorAction.OPEN_EXTERNALLY -> R.string.error_action_open_external
            ErrorAction.CANCEL_RECOVERY -> R.string.error_action_cancel_recovery
        }
    )

    override fun detailsLabel(): String = context.getString(R.string.error_details)

    override fun recovering(attempt: Int, seconds: Int): String = context.getString(R.string.error_recovering, attempt, seconds)

    override fun reconnecting(): String = context.getString(R.string.error_reconnecting)

    override fun waitingForNetwork(): String = context.getString(R.string.error_waiting_network)

    override fun theme() = WebViewExTheme(
        background = color(R.color.brandBackground),
        surface = color(R.color.brandBackgroundElevated),
        text = color(R.color.primaryText),
        mutedText = color(R.color.secondaryText),
        accent = color(R.color.brandPrimary),
        accentText = color(R.color.primaryText),
        secondaryAccent = color(R.color.brandSecondary),
        statusText = color(R.color.brandAuxAmber),
        iconTint = color(R.color.brandAuxPurple)
    )

    private fun color(id: Int) = context.resources.getColor(id, context.theme)
}
