package proto.media.fiesta.features.domain.core.settings

object CarToolbarAutoHide {

    fun canHide(autoHideEnabled: Boolean, isOverlayOpen: Boolean, isFullScreen: Boolean): Boolean =
        autoHideEnabled && !isOverlayOpen && !isFullScreen
}
