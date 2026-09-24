package proto.media.fiesta.support.webviewex.navigation

import proto.media.fiesta.support.search.testEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SchemePolicyTest {

    private val guessed = ResolvedAddress("https://router.local", schemeGuessed = true, isSearch = false)

    // WebViewEx.loadUrl(url) resolves the address and applies SchemePolicy before ever calling the
    // real WebView.loadUrl — this is the composition that closes Plano 50's loadUrl furo.
    @Test
    fun loadUrlGoesThroughHttpsFirst() {
        val address = AddressResolver.resolveDetailed("youtube.com", testEngine("google")::searchUrl)
        assertEquals("https://youtube.com", SchemePolicy.initialUrl(address, remembered = null))
    }

    @Test
    fun loadUrlHonoursARememberedHttpFallback() {
        val address = AddressResolver.resolveDetailed("youtube.com", testEngine("google")::searchUrl)
        assertEquals("http://youtube.com", SchemePolicy.initialUrl(address, remembered = Scheme.HTTP))
    }

    private fun transaction(url: String = "https://router.local", guessed: Boolean = true, fellBack: Boolean = false) =
        NavigationTransaction(1, NavigationOrigin.APP, "router.local", url, schemeGuessed = guessed, fellBackToHttp = fellBack)

    @Test
    fun guessedInputStartsOnHttpsWhenNothingIsRemembered() {
        assertEquals("https://router.local", SchemePolicy.initialUrl(guessed, null))
    }

    @Test
    fun guessedInputStartsOnHttpWhenHttpWasRemembered() {
        assertEquals("http://router.local", SchemePolicy.initialUrl(guessed, Scheme.HTTP))
    }

    @Test
    fun explicitHttpsIsNeverDowngradedByMemory() {
        val explicit = ResolvedAddress("https://router.local", schemeGuessed = false, isSearch = false)
        assertEquals("https://router.local", SchemePolicy.initialUrl(explicit, Scheme.HTTP))
    }

    @Test
    fun connectionFailureOfGuessedHttpsFallsBackToHttp() {
        assertEquals("http://router.local", SchemePolicy.fallbackUrl(transaction(), NavigationFailure.ConnectionRefused, false))
        assertEquals("http://router.local", SchemePolicy.fallbackUrl(transaction(), NavigationFailure.DnsFailure, false))
        assertEquals("http://router.local", SchemePolicy.fallbackUrl(transaction(), NavigationFailure.SslError, false))
    }

    @Test
    fun fallbackHappensOnlyOnce() {
        assertNull(SchemePolicy.fallbackUrl(transaction(fellBack = true), NavigationFailure.ConnectionRefused, false))
    }

    @Test
    fun explicitHttpsNeverFallsBack() {
        assertNull(SchemePolicy.fallbackUrl(transaction(guessed = false), NavigationFailure.ConnectionRefused, false))
    }

    @Test
    fun pinnedHostNeverFallsBack() {
        assertNull(SchemePolicy.fallbackUrl(transaction(), NavigationFailure.ConnectionRefused, true))
    }

    @Test
    fun offlineAndTimeoutDoNotTriggerFallback() {
        assertNull(SchemePolicy.fallbackUrl(transaction(), NavigationFailure.Offline, false))
        assertNull(SchemePolicy.fallbackUrl(transaction(), NavigationFailure.Timeout, false))
    }

    @Test
    fun onlyGuessedNavigationsUpdateTheMemory() {
        assertEquals(Scheme.HTTP, SchemePolicy.schemeToRemember(transaction(), "http://router.local/"))
        assertNull(SchemePolicy.schemeToRemember(transaction(guessed = false), "http://router.local/"))
    }

    @Test
    fun httpIsInsecure() {
        assertTrue(SchemePolicy.isInsecure("http://a.com"))
        assertFalse(SchemePolicy.isInsecure("https://a.com"))
    }

    @Test
    fun memoryRemembersAndPinsPerHost() {
        val store = object : SchemeStore {
            val values = mutableMapOf<String, String>()
            override fun read(host: String) = values[host]
            override fun write(host: String, value: String) { values[host] = value }
        }
        val memory = SchemeMemory(store)
        memory.remember("Router.local", Scheme.HTTP)
        assertEquals(Scheme.HTTP, memory.remembered("router.local"))
        memory.pinToHttps("router.local")
        memory.remember("router.local", Scheme.HTTP)
        assertEquals(Scheme.HTTPS, memory.remembered("router.local"))
        assertTrue(memory.isPinnedToHttps("router.local"))
    }
}
