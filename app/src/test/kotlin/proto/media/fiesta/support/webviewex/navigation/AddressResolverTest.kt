package proto.media.fiesta.support.webviewex.navigation

import proto.media.fiesta.support.search.testEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class AddressResolverTest {

    private val google = testEngine("google")
    private val youtube = testEngine("youtube")

    @Test
    fun bareDomainGetsHttpsScheme() {
        assertEquals("https://youtube.com", AddressResolver.resolve("youtube.com", google::searchUrl))
    }

    @Test
    fun localhostGetsHttpsScheme() {
        assertEquals("https://localhost", AddressResolver.resolve("localhost", google::searchUrl))
    }

    @Test
    fun textWithSpaceIsSearched() {
        assertEquals("https://www.google.com/search?q=a+b", AddressResolver.resolve("a b", google::searchUrl))
    }

    @Test
    fun singleWordIsSearched() {
        assertEquals("https://www.google.com/search?q=foo", AddressResolver.resolve("foo", google::searchUrl))
    }

    @Test
    fun urlWithSchemeIsUsedAsIs() {
        assertEquals("http://x", AddressResolver.resolve("http://x", google::searchUrl))
    }

    @Test
    fun hostWithPortGetsHttpsScheme() {
        assertEquals("https://192.168.0.1:8080", AddressResolver.resolve("192.168.0.1:8080", google::searchUrl))
    }

    @Test
    fun searchTermGoesToTheGivenEngine() {
        assertEquals("https://www.youtube.com/results?search_query=coldplay", AddressResolver.resolve("coldplay", youtube::searchUrl))
    }

    @Test
    fun addressStillWorksWithAnyEngine() {
        assertEquals("https://example.com", AddressResolver.resolve("example.com", youtube::searchUrl))
    }

    @Test
    fun bareDomainIsMarkedAsGuessedScheme() {
        assertEquals(true, AddressResolver.resolveDetailed("router.local", google::searchUrl).schemeGuessed)
    }

    @Test
    fun explicitSchemeIsNotGuessed() {
        assertEquals(false, AddressResolver.resolveDetailed("http://router.local", google::searchUrl).schemeGuessed)
    }

    @Test
    fun searchIsNotGuessedScheme() {
        val resolved = AddressResolver.resolveDetailed("cats", google::searchUrl)
        assertEquals(false, resolved.schemeGuessed)
        assertEquals(true, resolved.isSearch)
    }
}
