package proto.media.fiesta.features.domain.core.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class DonatePageTest {

    @Test
    fun `lowercase Portuguese gets the Portuguese page`() {
        assertEquals("https://println.github.io/fiesta/pt-br/donate.html", DonatePage.urlFor("pt"))
    }

    @Test
    fun `uppercase Portuguese also gets the Portuguese page`() {
        assertEquals("https://println.github.io/fiesta/pt-br/donate.html", DonatePage.urlFor("PT"))
    }

    @Test
    fun `English gets the English page`() {
        assertEquals("https://println.github.io/fiesta/donate.html", DonatePage.urlFor("en"))
    }

    @Test
    fun `any other language falls back to the English page`() {
        assertEquals("https://println.github.io/fiesta/donate.html", DonatePage.urlFor("es"))
    }

    @Test
    fun `empty language falls back to the English page`() {
        assertEquals("https://println.github.io/fiesta/donate.html", DonatePage.urlFor(""))
    }
}
