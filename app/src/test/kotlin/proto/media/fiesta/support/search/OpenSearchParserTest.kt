package proto.media.fiesta.support.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OpenSearchParserTest {

    @Test
    fun googleAssetShortNameIsReadByJvmXmlParser() {
        assertEquals("Google", assetDescription("google").shortName)
    }

    @Test
    fun googleAssetDescription() {
        assertEquals(
            SearchEngineDescription(
                id = "google",
                version = 1,
                shortName = "Google",
                searchTemplate = "https://www.google.com/search?q={searchTerms}",
                suggestionsTemplate = "https://suggestqueries.google.com/complete/search?client=chrome&q={searchTerms}",
                hosts = listOf("google.com", "*.google.com")
            ),
            assetDescription("google")
        )
    }

    @Test
    fun youtubeAssetDescription() {
        assertEquals(
            SearchEngineDescription(
                id = "youtube",
                version = 1,
                shortName = "YouTube",
                searchTemplate = "https://www.youtube.com/results?search_query={searchTerms}",
                suggestionsTemplate = "https://suggestqueries.google.com/complete/search?client=firefox&ds=yt&q={searchTerms}",
                hosts = listOf("youtube.com", "*.youtube.com", "youtu.be")
            ),
            assetDescription("youtube")
        )
    }

    @Test
    fun minimalDescriptionUsesDefaults() {
        val description = parse(minimal())
        assertEquals(1, description.version)
        assertNull(description.suggestionsTemplate)
        assertEquals(emptyList<String>(), description.hosts)
    }

    @Test
    fun unknownElementsAreIgnored() {
        val description = parse(minimal(extra = "<Image>x</Image><Description>d</Description><Tags>t</Tags>"))
        assertEquals("Engine", description.shortName)
    }

    @Test
    fun explicitVersionIsRead() {
        assertEquals(3, parse(minimal(extra = "<cs:Version>3</cs:Version>")).version)
    }

    @Test
    fun lowercaseUtf8EncodingIsAccepted() {
        assertEquals("Engine", parse(minimal(encoding = "<InputEncoding>utf-8</InputEncoding>")).shortName)
    }

    @Test(expected = InvalidSearchEngineException::class)
    fun rejectsOtherRoot() {
        parse("<Other xmlns=\"$OS\"/>")
    }

    @Test(expected = InvalidSearchEngineException::class)
    fun rejectsRootOutsideOpenSearchNamespace() {
        parse(minimal().replace("xmlns=\"$OS\"", "xmlns=\"urn:other\""))
    }

    @Test(expected = InvalidSearchEngineException::class)
    fun rejectsMalformedXml() {
        parse("<OpenSearchDescription")
    }

    @Test(expected = InvalidSearchEngineException::class)
    fun rejectsMissingShortName() {
        parse(minimal(shortName = ""))
    }

    @Test(expected = InvalidSearchEngineException::class)
    fun rejectsBlankShortName() {
        parse(minimal(shortName = "<ShortName>  </ShortName>"))
    }

    @Test(expected = InvalidSearchEngineException::class)
    fun rejectsNonUtf8Encoding() {
        parse(minimal(encoding = "<InputEncoding>ISO-8859-1</InputEncoding>"))
    }

    @Test(expected = InvalidSearchEngineException::class)
    fun rejectsMissingSearchUrl() {
        parse(minimal(urls = ""))
    }

    @Test(expected = InvalidSearchEngineException::class)
    fun rejectsTwoSearchUrls() {
        parse(minimal(urls = SEARCH_URL + SEARCH_URL))
    }

    @Test(expected = InvalidSearchEngineException::class)
    fun rejectsTwoSuggestionUrls() {
        parse(minimal(urls = SEARCH_URL + SUGGESTIONS_URL + SUGGESTIONS_URL))
    }

    @Test
    fun ignoresOtherUrlTypes() {
        val description = parse(minimal(urls = SEARCH_URL + "<Url type=\"application/rss+xml\" template=\"http://x/{y}\"/>"))
        assertEquals("https://e.com/?q={searchTerms}", description.searchTemplate)
    }

    @Test(expected = InvalidSearchEngineException::class)
    fun rejectsUrlWithParam() {
        parse(minimal(urls = "<Url type=\"text/html\" template=\"https://e.com/?q={searchTerms}\"><Param name=\"a\" value=\"b\"/></Url>"))
    }

    @Test(expected = InvalidSearchEngineException::class)
    fun rejectsNonHttpsTemplate() {
        parse(minimal(urls = "<Url type=\"text/html\" template=\"http://e.com/?q={searchTerms}\"/>"))
    }

    @Test(expected = InvalidSearchEngineException::class)
    fun rejectsTemplateWithoutSearchTerms() {
        parse(minimal(urls = "<Url type=\"text/html\" template=\"https://e.com/\"/>"))
    }

    @Test(expected = InvalidSearchEngineException::class)
    fun rejectsTemplateWithSearchTermsTwice() {
        parse(minimal(urls = "<Url type=\"text/html\" template=\"https://e.com/?q={searchTerms}&amp;r={searchTerms}\"/>"))
    }

    @Test(expected = InvalidSearchEngineException::class)
    fun rejectsTemplateWithOtherPlaceholder() {
        parse(minimal(urls = "<Url type=\"text/html\" template=\"https://e.com/?q={searchTerms}&amp;p={startPage?}\"/>"))
    }

    @Test(expected = InvalidSearchEngineException::class)
    fun rejectsInvalidSuggestionsTemplate() {
        parse(minimal(urls = SEARCH_URL + "<Url type=\"application/x-suggestions+json\" template=\"http://e.com/?q={searchTerms}\"/>"))
    }

    @Test(expected = InvalidSearchEngineException::class)
    fun rejectsMissingId() {
        parse(minimal(id = ""))
    }

    @Test(expected = InvalidSearchEngineException::class)
    fun rejectsIdWithUppercase() {
        parse(minimal(id = "<cs:Id>Engine</cs:Id>"))
    }

    @Test(expected = InvalidSearchEngineException::class)
    fun rejectsIdLongerThan32() {
        parse(minimal(id = "<cs:Id>${"a".repeat(33)}</cs:Id>"))
    }

    @Test(expected = InvalidSearchEngineException::class)
    fun rejectsZeroVersion() {
        parse(minimal(extra = "<cs:Version>0</cs:Version>"))
    }

    @Test(expected = InvalidSearchEngineException::class)
    fun rejectsNonNumericVersion() {
        parse(minimal(extra = "<cs:Version>one</cs:Version>"))
    }

    @Test(expected = InvalidSearchEngineException::class)
    fun rejectsInvalidHost() {
        parse(minimal(extra = "<cs:Host>a.*.com</cs:Host>"))
    }

    private fun parse(xml: String): SearchEngineDescription =
        OpenSearchParser.parse(xml.byteInputStream(Charsets.UTF_8))

    private fun minimal(
        shortName: String = "<ShortName>Engine</ShortName>",
        encoding: String = "",
        urls: String = SEARCH_URL,
        id: String = "<cs:Id>engine</cs:Id>",
        extra: String = ""
    ): String =
        "<OpenSearchDescription xmlns=\"$OS\" xmlns:cs=\"$CS\">$shortName$encoding$urls$id$extra</OpenSearchDescription>"

    private companion object {
        const val OS = "http://a9.com/-/spec/opensearch/1.1/"
        const val CS = "urn:fiesta:search:1"
        const val SEARCH_URL = "<Url type=\"text/html\" template=\"https://e.com/?q={searchTerms}\"/>"
        const val SUGGESTIONS_URL = "<Url type=\"application/x-suggestions+json\" template=\"https://e.com/s?q={searchTerms}\"/>"
    }
}
