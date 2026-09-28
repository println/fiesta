package proto.media.fiesta.support.webviewex.navigation

enum class NavigationOrigin { APP, PAGE, HISTORY, RETRY, RECOVERY }

enum class NavigationState { PENDING, COMMITTED, SUCCEEDED, FAILED, ABORTED }

data class NavigationTransaction(
    val id: Long,
    val origin: NavigationOrigin,
    val input: String,
    val requestedUrl: String,
    val currentUrl: String = requestedUrl,
    val method: String = "GET",
    val redirects: List<String> = emptyList(),
    val schemeGuessed: Boolean = false,
    val fellBackToHttp: Boolean = false,
    val recoveryAttempts: Int = 0,
    val state: NavigationState = NavigationState.PENDING,
    val documentCommitted: Boolean = false
) {
    val isTerminal: Boolean
        get() = state == NavigationState.SUCCEEDED || state == NavigationState.FAILED || state == NavigationState.ABORTED

    fun redirectedTo(url: String) = copy(currentUrl = url, redirects = redirects + url)

    fun committed() = copy(state = NavigationState.COMMITTED)

    fun painted() = copy(documentCommitted = true)

    fun succeeded() = copy(state = NavigationState.SUCCEEDED)

    fun failed() = copy(state = NavigationState.FAILED)

    fun aborted() = copy(state = NavigationState.ABORTED)

    fun involves(url: String): Boolean {
        val target = normalize(url)
        return target == normalize(requestedUrl) || target == normalize(currentUrl) || redirects.any { normalize(it) == target }
    }

    companion object {
        fun normalize(url: String): String {
            val withoutFragment = url.substringBefore('#')
            val schemeEnd = withoutFragment.indexOf("://")
            if (schemeEnd < 0) return withoutFragment
            val authorityStart = schemeEnd + 3
            val pathStart = withoutFragment.indexOfAny(charArrayOf('/', '?'), authorityStart)
            return when {
                pathStart < 0 -> "$withoutFragment/"
                withoutFragment[pathStart] == '?' ->
                    withoutFragment.substring(0, pathStart) + "/" + withoutFragment.substring(pathStart)
                else -> withoutFragment
            }
        }
    }
}
