package proto.media.fiesta.support.plugins.activation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivationOrderCodecTest {

    @Test
    fun encodeThenDecodeRoundTrips() {
        val ids = listOf("youtube", "generic")
        assertEquals(ids, ActivationOrderCodec.decode(ActivationOrderCodec.encode(ids)))
    }

    @Test
    fun decodeNullReturnsEmptyList() {
        assertTrue(ActivationOrderCodec.decode(null).isEmpty())
    }

    @Test
    fun decodeEmptyStringReturnsEmptyList() {
        assertTrue(ActivationOrderCodec.decode("").isEmpty())
    }

    @Test
    fun decodeDiscardsEmptyIds() {
        assertEquals(listOf("youtube", "generic"), ActivationOrderCodec.decode("youtube,,generic,"))
    }
}
