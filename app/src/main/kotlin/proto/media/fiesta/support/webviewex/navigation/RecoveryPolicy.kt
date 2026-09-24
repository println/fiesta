package proto.media.fiesta.support.webviewex.navigation

class RecoveryPolicy(
    private val backoffMillis: List<Long> = listOf(1_000L, 2_000L, 5_000L, 15_000L, 30_000L),
    private val maxNetworkWakeups: Int = 3,
    private val maxRenderRecoveries: Int = 3,
    private val renderRecoveryWindowMillis: Long = 60_000L
) {

    enum class GiveUpReason { CANCELLED, NOT_TRANSIENT, DOCUMENT_ALIVE, EXHAUSTED }

    sealed class Decision {
        data class RetryAfter(val millis: Long, val attempt: Int) : Decision()
        object WaitForNetwork : Decision()
        data class GiveUp(val reason: GiveUpReason) : Decision()
    }

    data class Context(
        val requestedUrlStillCurrent: Boolean,
        val viewAlive: Boolean,
        val online: Boolean,
        val networkWakeups: Int
    )

    fun isTransient(failure: NavigationFailure): Boolean = when (failure) {
        NavigationFailure.Offline, NavigationFailure.Timeout, NavigationFailure.DnsFailure,
        NavigationFailure.ConnectionRefused -> true
        is NavigationFailure.HttpStatus -> failure.code in 500..599
        else -> false
    }

    fun decide(failure: NavigationFailure, transaction: NavigationTransaction, context: Context): Decision {
        if (!context.viewAlive || !context.requestedUrlStillCurrent) return Decision.GiveUp(GiveUpReason.CANCELLED)
        if (!isTransient(failure)) return Decision.GiveUp(GiveUpReason.NOT_TRANSIENT)
        if (transaction.documentCommitted) return Decision.GiveUp(GiveUpReason.DOCUMENT_ALIVE)
        val attempts = transaction.recoveryAttempts
        val mustWaitForNetwork = failure == NavigationFailure.Offline || !context.online || attempts >= backoffMillis.size
        if (!mustWaitForNetwork) return Decision.RetryAfter(backoffMillis[attempts], attempts + 1)
        return if (context.networkWakeups >= maxNetworkWakeups) {
            Decision.GiveUp(GiveUpReason.EXHAUSTED)
        } else {
            Decision.WaitForNetwork
        }
    }

    fun allowsRenderProcessRecovery(previousRecoveriesMillis: List<Long>, nowMillis: Long): Boolean =
        previousRecoveriesMillis.count { nowMillis - it < renderRecoveryWindowMillis } < maxRenderRecoveries
}
