package proto.media.fiesta.features.domain.core.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppVersionTest {

    @Test
    fun `tag with v prefix parses like the plain version`() {
        assertEquals(0, AppVersion.parse("v1.0.2")!!.compareTo(AppVersion.parse("1.0.2")!!))
    }

    @Test
    fun `higher patch is newer`() {
        assertTrue(AppVersion.parse("1.0.2")!! > AppVersion.parse("1.0.1")!!)
    }

    @Test
    fun `numeric parts compare as numbers, not text`() {
        assertTrue(AppVersion.parse("1.0.10")!! > AppVersion.parse("1.0.9")!!)
    }

    @Test
    fun `missing parts count as zero`() {
        assertEquals(0, AppVersion.parse("1.1")!!.compareTo(AppVersion.parse("1.1.0")!!))
    }

    @Test
    fun `text is not a version`() {
        assertNull(AppVersion.parse("v1.0.2-beta"))
        assertNull(AppVersion.parse("latest"))
        assertNull(AppVersion.parse(""))
        assertNull(AppVersion.parse("1..2"))
        assertNull(AppVersion.parse("1.-1"))
    }

    @Test
    fun `prints without the v prefix`() {
        assertEquals("1.0.2", AppVersion.parse("v1.0.2").toString())
    }
}
