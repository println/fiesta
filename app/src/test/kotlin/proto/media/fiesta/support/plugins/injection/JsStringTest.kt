package proto.media.fiesta.support.plugins.injection

import org.junit.Assert.assertEquals
import org.junit.Test

class JsStringTest {

    @Test
    fun wrapsPlainTextInDoubleQuotes() {
        assertEquals("\"lofi beats\"", JsString.quote("lofi beats"))
    }

    @Test
    fun escapesQuotesBackslashesAndSlashes() {
        assertEquals("\"say \\\"hi\\\" \\\\ <\\/script>\"", JsString.quote("say \"hi\" \\ </script>"))
    }

    @Test
    fun escapesNamedControlCharacters() {
        assertEquals("\"\\t\\b\\n\\r\\f\"", JsString.quote("\t\b\n\r\u000C"))
    }

    @Test
    fun escapesOtherControlCharactersAsUnicode() {
        assertEquals("\"\\u0000\\u001f\"", JsString.quote("\u0000\u001F"))
    }

    @Test
    fun keepsNonAsciiText() {
        assertEquals("\"ação ♪\"", JsString.quote("ação ♪"))
    }
}
