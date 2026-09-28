package proto.media.fiesta.features.domain.core.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SettingsStorageTest {

    @Test
    fun `phone, car and bare hosts for the same site write the same key`() {
        val phoneKey = SettingsStorage.keyFor("desktop_", "m.youtube.com")
        val carKey = SettingsStorage.keyFor("desktop_", "www.youtube.com")
        val bareKey = SettingsStorage.keyFor("desktop_", "youtube.com")

        assertEquals("desktop_youtube.com", phoneKey)
        assertEquals(phoneKey, carKey)
        assertEquals(phoneKey, bareKey)
    }

    @Test
    fun `host comparison is case-insensitive`() {
        assertEquals(
            SettingsStorage.keyFor("desktop_", "YouTube.com"),
            SettingsStorage.keyFor("desktop_", "youtube.com")
        )
    }

    @Test
    fun `different sites get different keys`() {
        val youtube = SettingsStorage.keyFor("desktop_", "m.youtube.com")
        val google = SettingsStorage.keyFor("desktop_", "www.google.com")

        assertEquals("desktop_youtube.com", youtube)
        assertEquals("desktop_google.com", google)
    }

    @Test
    fun `prefix keeps ad-block and desktop overrides on separate keys`() {
        val desktop = SettingsStorage.keyFor("desktop_", "m.youtube.com")
        val adBlock = SettingsStorage.keyFor("adblock_", "m.youtube.com")

        assertEquals("desktop_youtube.com", desktop)
        assertEquals("adblock_youtube.com", adBlock)
    }

    @Test
    fun `null, empty and blank hosts have no key`() {
        assertNull(SettingsStorage.keyFor("desktop_", null))
        assertNull(SettingsStorage.keyFor("desktop_", ""))
        assertNull(SettingsStorage.keyFor("desktop_", "   "))
    }

    @Test
    fun `any subdomain of a site shares its key`() {
        assertEquals("youtube.com", SettingsStorage.domainKey("music.youtube.com"))
        assertEquals("youtube.com", SettingsStorage.domainKey("www.m.youtube.com"))
        assertEquals("plex.tv", SettingsStorage.domainKey("app.plex.tv"))
    }

    @Test
    fun `multi-label public suffixes are not mistaken for the domain`() {
        assertEquals("globo.com.br", SettingsStorage.domainKey("g1.globo.com.br"))
        assertEquals("globo.com.br", SettingsStorage.domainKey("www.globo.com.br"))
        assertEquals("bbc.co.uk", SettingsStorage.domainKey("www.bbc.co.uk"))
    }

    @Test
    fun `hosts without a registrable domain keep their own key`() {
        assertEquals("localhost", SettingsStorage.domainKey("localhost"))
        assertEquals("192.168.0.1", SettingsStorage.domainKey("192.168.0.1"))
    }
}
