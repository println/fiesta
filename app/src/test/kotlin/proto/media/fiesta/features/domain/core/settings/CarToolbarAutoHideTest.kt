package proto.media.fiesta.features.domain.core.settings

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CarToolbarAutoHideTest {

    @Test
    fun canHideOnlyWhenEnabledAndNothingElseIsOpen() {
        assertTrue(CarToolbarAutoHide.canHide(autoHideEnabled = true, isOverlayOpen = false, isFullScreen = false))
    }

    @Test
    fun cannotHideWhenDisabled() {
        assertFalse(CarToolbarAutoHide.canHide(autoHideEnabled = false, isOverlayOpen = false, isFullScreen = false))
    }

    @Test
    fun cannotHideWhileAnOverlayScreenIsOpen() {
        assertFalse(CarToolbarAutoHide.canHide(autoHideEnabled = true, isOverlayOpen = true, isFullScreen = false))
    }

    @Test
    fun cannotHideInFullScreen() {
        assertFalse(CarToolbarAutoHide.canHide(autoHideEnabled = true, isOverlayOpen = false, isFullScreen = true))
    }
}
