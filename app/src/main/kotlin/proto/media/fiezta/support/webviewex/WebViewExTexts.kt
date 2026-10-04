package proto.media.fiezta.support.webviewex

import proto.media.fiezta.support.webviewex.navigation.NavigationFailure

enum class ErrorAction(val id: String) {
    RETRY("retry"),
    RETRY_HTTP("retryHttp"),
    GO_BACK("goBack"),
    OPEN_EXTERNALLY("openExternally"),
    CANCEL_RECOVERY("cancelRecovery")
}

interface WebViewExTexts {
    fun title(failure: NavigationFailure): String
    fun message(failure: NavigationFailure): String
    fun actionLabel(action: ErrorAction): String
    fun detailsLabel(): String
    fun recovering(attempt: Int, seconds: Int): String
    fun reconnecting(): String
    fun waitingForNetwork(): String
    fun theme(): WebViewExTheme
}
