package proto.media.fiesta.features.domain.core.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCheckTest {

    @Test
    fun `newer release is notified`() {
        assertTrue(UpdateCheck.shouldNotify("1.0.1", "v1.0.2"))
    }

    @Test
    fun `same release is not notified`() {
        assertFalse(UpdateCheck.shouldNotify("1.0.1", "v1.0.1"))
    }

    @Test
    fun `older release is not notified`() {
        assertFalse(UpdateCheck.shouldNotify("1.0.2", "v1.0.1"))
    }

    @Test
    fun `unreadable tag is not notified`() {
        assertFalse(UpdateCheck.shouldNotify("1.0.1", "nightly"))
    }
}
