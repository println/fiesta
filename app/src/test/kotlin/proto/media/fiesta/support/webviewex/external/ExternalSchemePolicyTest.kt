package proto.media.fiesta.support.webviewex.external

import org.junit.Assert.assertEquals
import org.junit.Test

class ExternalSchemePolicyTest {

    @Test
    fun webSchemesAreNotExternal() {
        assertEquals(UrlKind.WEB, ExternalSchemePolicy.classify("https://a.com"))
        assertEquals(UrlKind.WEB, ExternalSchemePolicy.classify("about:blank"))
        assertEquals(UrlKind.WEB, ExternalSchemePolicy.classify("file:///android_asset/x.html"))
    }

    @Test
    fun externalSchemesAreClassified() {
        assertEquals(UrlKind.INTENT_URI, ExternalSchemePolicy.classify("intent://x#Intent;end"))
        assertEquals(UrlKind.MAILTO, ExternalSchemePolicy.classify("mailto:a@b.c"))
        assertEquals(UrlKind.TEL, ExternalSchemePolicy.classify("tel:123"))
        assertEquals(UrlKind.SMS, ExternalSchemePolicy.classify("sms:123"))
        assertEquals(UrlKind.MARKET, ExternalSchemePolicy.classify("market://details?id=x"))
        assertEquals(UrlKind.ANDROID_APP, ExternalSchemePolicy.classify("android-app://x"))
        assertEquals(UrlKind.OTHER_EXTERNAL, ExternalSchemePolicy.classify("spotify:track:1"))
    }

    @Test
    fun grantAndNewDocumentFlagsAreDropped() {
        val dangerous = 0x1 or 0x2 or 0x40 or 0x80 or 0x00080000
        val harmless = 0x10000000
        assertEquals(harmless, ExternalSchemePolicy.sanitizeFlags(dangerous or harmless))
    }

    @Test
    fun cascadeLaunchesThenFallsBackThenMarketThenGivesUp() {
        assertEquals(ExternalResolution.Launch, ExternalSchemePolicy.resolve(true, "https://f.com", "pkg"))
        assertEquals(ExternalResolution.LoadFallback("https://f.com"), ExternalSchemePolicy.resolve(false, "https://f.com", "pkg"))
        assertEquals(ExternalResolution.OpenMarket("pkg"), ExternalSchemePolicy.resolve(false, null, "pkg"))
        assertEquals(ExternalResolution.NotInstalled, ExternalSchemePolicy.resolve(false, null, null))
    }

    @Test
    fun fallbackThatIsNotWebIsRejected() {
        assertEquals(ExternalResolution.NotInstalled, ExternalSchemePolicy.resolve(false, "intent://loop", null))
    }
}
