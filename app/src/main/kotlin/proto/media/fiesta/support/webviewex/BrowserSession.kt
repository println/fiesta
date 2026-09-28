package proto.media.fiesta.support.webviewex

import proto.media.fiesta.support.webviewex.navigation.NavigationFailure

data class BrowserSession(
    val url: String?,
    val host: String?,
    val title: String?,
    val isSecure: Boolean,
    val isLoading: Boolean,
    val failure: NavigationFailure?,
    val isErrorDocument: Boolean
)

data class LoadingState(val isLoading: Boolean, val progress: Int) {
    companion object {
        val IDLE = LoadingState(isLoading = false, progress = 0)
    }
}

data class HistoryEntry(val rawIndex: Int, val url: String, val title: String?)

data class BrowserHistory(val entries: List<HistoryEntry>, val currentPosition: Int) {
    companion object {
        val EMPTY = BrowserHistory(emptyList(), -1)
    }
}
