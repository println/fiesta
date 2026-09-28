package proto.media.fiesta.support.webviewex.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationFailureTest {

    @Test
    fun cleartextIsRecognisedFromTheDescription() {
        assertEquals(NavigationFailure.CleartextBlocked, NavigationFailure.classify(-1, "net::ERR_CLEARTEXT_NOT_PERMITTED", true))
    }

    @Test
    fun disconnectedNetworkIsOffline() {
        assertEquals(NavigationFailure.Offline, NavigationFailure.classify(-2, "net::ERR_NAME_NOT_RESOLVED", false))
        assertEquals(NavigationFailure.Offline, NavigationFailure.classify(-6, "net::ERR_INTERNET_DISCONNECTED", true))
    }

    @Test
    fun lookupFailureOnlineIsDns() {
        assertEquals(NavigationFailure.DnsFailure, NavigationFailure.classify(-2, "net::ERR_NAME_NOT_RESOLVED", true))
    }

    @Test
    fun certificateProblemsAreSsl() {
        assertEquals(NavigationFailure.SslError, NavigationFailure.classify(-11, "net::ERR_SSL_PROTOCOL_ERROR", true))
        assertEquals(NavigationFailure.SslError, NavigationFailure.classify(-1, "net::ERR_CERT_DATE_INVALID", true))
    }

    @Test
    fun timeoutAndRefusalAreDistinct() {
        assertEquals(NavigationFailure.Timeout, NavigationFailure.classify(-8, "net::ERR_CONNECTION_TIMED_OUT", true))
        assertEquals(NavigationFailure.ConnectionRefused, NavigationFailure.classify(-6, "net::ERR_CONNECTION_REFUSED", true))
    }

    @Test
    fun blockedByClientIsContentFilter() {
        assertEquals(NavigationFailure.BlockedByContentFilter, NavigationFailure.classify(-1, "net::ERR_BLOCKED_BY_CLIENT", true))
    }

    @Test
    fun unsupportedSchemeIsRecognisedFromTheCode() {
        assertEquals(NavigationFailure.UnsupportedScheme, NavigationFailure.classify(-10, "net::ERR_UNKNOWN_URL_SCHEME", true))
    }
}

class UserAgentPolicyTest {

    private val default = "Mozilla/5.0 (Linux; Android 14; Pixel; wv) AppleWebKit Chrome/120"

    @Test
    fun desktopWinsOverEverything() {
        assertEquals(UserAgentPolicy.DESKTOP_USER_AGENT, UserAgentPolicy.userAgentFor(true, true, default))
    }

    @Test
    fun mobileStripsTheWebViewMarkerWhenAsked() {
        assertFalse("; wv" in UserAgentPolicy.userAgentFor(false, true, default))
    }

    @Test
    fun mobileKeepsTheDefaultOtherwise() {
        assertEquals(default, UserAgentPolicy.userAgentFor(false, false, default))
    }
}

class RecoveryPolicyTest {

    private val policy = RecoveryPolicy()
    private val transaction = NavigationTransaction(1, NavigationOrigin.APP, "a", "https://a.com/")
    private val context = RecoveryPolicy.Context(requestedUrlStillCurrent = true, viewAlive = true, online = true, networkWakeups = 0)

    @Test
    fun onlyTransientFailuresAreRecoverable() {
        assertTrue(policy.isTransient(NavigationFailure.Offline))
        assertTrue(policy.isTransient(NavigationFailure.Timeout))
        assertTrue(policy.isTransient(NavigationFailure.DnsFailure))
        assertTrue(policy.isTransient(NavigationFailure.HttpStatus(503)))
        assertFalse(policy.isTransient(NavigationFailure.SslError))
        assertFalse(policy.isTransient(NavigationFailure.HttpStatus(404)))
        assertFalse(policy.isTransient(NavigationFailure.CleartextBlocked))
        assertFalse(policy.isTransient(NavigationFailure.UnsupportedScheme))
    }

    @Test
    fun backoffGrowsWithAttempts() {
        val first = policy.decide(NavigationFailure.Timeout, transaction, context)
        val third = policy.decide(NavigationFailure.Timeout, transaction.copy(recoveryAttempts = 2), context)
        assertEquals(RecoveryPolicy.Decision.RetryAfter(1_000L, 1), first)
        assertEquals(RecoveryPolicy.Decision.RetryAfter(5_000L, 3), third)
    }

    @Test
    fun nonTransientFailureGivesUp() {
        val decision = policy.decide(NavigationFailure.SslError, transaction, context)
        assertEquals(RecoveryPolicy.Decision.GiveUp(RecoveryPolicy.GiveUpReason.NOT_TRANSIENT), decision)
    }

    @Test
    fun paintedDocumentIsNeverReloaded() {
        val decision = policy.decide(NavigationFailure.Timeout, transaction.painted(), context)
        assertEquals(RecoveryPolicy.Decision.GiveUp(RecoveryPolicy.GiveUpReason.DOCUMENT_ALIVE), decision)
    }

    @Test
    fun startedButUnpaintedDocumentStillRecovers() {
        val decision = policy.decide(NavigationFailure.Timeout, transaction.committed(), context)
        assertEquals(RecoveryPolicy.Decision.RetryAfter(1_000L, 1), decision)
    }

    @Test
    fun changedUrlOrDeadViewCancels() {
        val moved = policy.decide(NavigationFailure.Timeout, transaction, context.copy(requestedUrlStillCurrent = false))
        val dead = policy.decide(NavigationFailure.Timeout, transaction, context.copy(viewAlive = false))
        assertEquals(RecoveryPolicy.Decision.GiveUp(RecoveryPolicy.GiveUpReason.CANCELLED), moved)
        assertEquals(RecoveryPolicy.Decision.GiveUp(RecoveryPolicy.GiveUpReason.CANCELLED), dead)
    }

    @Test
    fun offlineWaitsForTheNetworkInsteadOfBackingOff() {
        assertEquals(RecoveryPolicy.Decision.WaitForNetwork, policy.decide(NavigationFailure.Offline, transaction, context))
        assertEquals(RecoveryPolicy.Decision.WaitForNetwork, policy.decide(NavigationFailure.Timeout, transaction, context.copy(online = false)))
    }

    @Test
    fun exhaustedAttemptsWaitForTheNetworkThenGiveUp() {
        val exhausted = transaction.copy(recoveryAttempts = 5)
        assertEquals(RecoveryPolicy.Decision.WaitForNetwork, policy.decide(NavigationFailure.Timeout, exhausted, context))
        val decision = policy.decide(NavigationFailure.Timeout, exhausted, context.copy(networkWakeups = 3))
        assertEquals(RecoveryPolicy.Decision.GiveUp(RecoveryPolicy.GiveUpReason.EXHAUSTED), decision)
    }

    @Test
    fun renderProcessRecoveriesAreCappedInAWindow() {
        assertTrue(policy.allowsRenderProcessRecovery(listOf(0L, 10_000L), 20_000L))
        assertFalse(policy.allowsRenderProcessRecovery(listOf(0L, 10_000L, 15_000L), 20_000L))
        assertTrue(policy.allowsRenderProcessRecovery(listOf(0L, 10_000L, 15_000L), 80_000L))
    }
}

class WindowPolicyTest {

    @Test
    fun sameTabOpensOnlyWithUserGesture() {
        assertEquals(WindowAction.OPEN_IN_SAME_TAB, WindowPolicy.decide(WindowMode.SAME_TAB, true))
        assertEquals(WindowAction.DENY, WindowPolicy.decide(WindowMode.SAME_TAB, false))
    }

    @Test
    fun denyAlwaysDenies() {
        assertEquals(WindowAction.DENY, WindowPolicy.decide(WindowMode.DENY, true))
    }

    @Test
    fun newTabFallsBackToSameTabForNow() {
        assertEquals(WindowAction.OPEN_IN_SAME_TAB, WindowPolicy.decide(WindowMode.NEW_TAB, true))
    }
}
