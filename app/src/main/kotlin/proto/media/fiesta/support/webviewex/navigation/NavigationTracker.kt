package proto.media.fiesta.support.webviewex.navigation

class NavigationTracker {

    var current: NavigationTransaction? = null
        private set

    private var nextId = 1L
    private val replacedTransactions = ArrayDeque<NavigationTransaction>()

    fun begin(
        origin: NavigationOrigin,
        input: String,
        url: String,
        method: String = "GET",
        schemeGuessed: Boolean = false,
        fellBackToHttp: Boolean = false,
        recoveryAttempts: Int = 0
    ): NavigationTransaction {
        replaceCurrent()
        val transaction = NavigationTransaction(
            id = nextId++,
            origin = origin,
            input = input,
            requestedUrl = url,
            method = method,
            schemeGuessed = schemeGuessed,
            fellBackToHttp = fellBackToHttp,
            recoveryAttempts = recoveryAttempts
        )
        current = transaction
        return transaction
    }

    fun redirect(url: String): NavigationTransaction? {
        val transaction = current?.takeUnless { it.isTerminal } ?: return null
        return transaction.redirectedTo(url).also { current = it }
    }

    fun onPageStarted(url: String): NavigationTransaction {
        val transaction = current
        if (transaction != null && !transaction.isTerminal && transaction.involves(url)) {
            return transaction.committed().copy(currentUrl = url).also { current = it }
        }
        return begin(NavigationOrigin.PAGE, url, url).committed().also { current = it }
    }

    fun onContentPainted(url: String?): NavigationTransaction? {
        val transaction = current?.takeUnless { it.isTerminal } ?: return null
        if (url != null && !transaction.involves(url)) return null
        return transaction.painted().also { current = it }
    }

    fun onPageFinished(url: String?): NavigationTransaction? {
        val transaction = current ?: return null
        if (transaction.state != NavigationState.COMMITTED) return null
        if (url != null && !transaction.involves(url)) return null
        return transaction.succeeded().also { current = it }
    }

    fun mainFrameFailure(url: String): NavigationTransaction? {
        val transaction = current?.takeUnless { it.isTerminal } ?: return null
        if (!transaction.involves(url) && wasReplaced(url)) return null
        return transaction.failed().also { current = it }
    }

    fun strictFailure(url: String): NavigationTransaction? {
        val transaction = current?.takeUnless { it.isTerminal } ?: return null
        if (!transaction.involves(url)) return null
        return transaction.failed().also { current = it }
    }

    fun abort() {
        val transaction = current?.takeUnless { it.isTerminal } ?: return
        val aborted = transaction.aborted()
        remember(aborted)
        current = aborted
    }

    fun involvesMainFrame(url: String?): Boolean = url != null && current?.involves(url) == true

    private fun replaceCurrent() {
        val previous = current ?: return
        if (!previous.isTerminal) remember(previous.aborted())
    }

    private fun remember(transaction: NavigationTransaction) {
        replacedTransactions.addLast(transaction)
        if (replacedTransactions.size > MAX_REMEMBERED) replacedTransactions.removeFirst()
    }

    private fun wasReplaced(url: String) = replacedTransactions.any { it.involves(url) }

    private companion object {
        const val MAX_REMEMBERED = 8
    }
}
