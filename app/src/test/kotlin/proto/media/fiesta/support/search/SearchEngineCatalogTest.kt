package proto.media.fiesta.support.search

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

class SearchEngineCatalogTest {

    private class DiskFiles(
        private val defaults: SearchEngineDefaults,
        private val renamed: Map<String, String> = emptyMap()
    ) : SearchEngineFiles {
        override fun readDefaults() = defaults

        override fun openDescription(engineId: String): InputStream {
            val xml = File("src/main/assets/search/$engineId.xml").readText()
            val renamedId = renamed[engineId] ?: return ByteArrayInputStream(xml.toByteArray())
            return ByteArrayInputStream(xml.replace("<cs:Id>$engineId</cs:Id>", "<cs:Id>$renamedId</cs:Id>").toByteArray())
        }
    }

    @Test
    fun loadsTheEnginesInTheDeclaredOrderWithTheDeclaredFallback() {
        val selector = SearchEngineCatalog.load(DiskFiles(SearchEngineDefaults(listOf("youtube", "google"), "google")))

        assertEquals(listOf("youtube", "google"), selector.engines.map { it.id })
        assertEquals("google", selector.fallback.id)
    }

    @Test(expected = IllegalStateException::class)
    fun rejectsAFallbackThatIsNotInTheOrder() {
        SearchEngineCatalog.load(DiskFiles(SearchEngineDefaults(listOf("youtube"), "google")))
    }

    @Test(expected = IllegalStateException::class)
    fun rejectsADescriptionWhoseIdDoesNotMatchItsFile() {
        val files = DiskFiles(SearchEngineDefaults(listOf("google"), "google"), renamed = mapOf("google" to "bing"))

        SearchEngineCatalog.load(files)
    }
}
