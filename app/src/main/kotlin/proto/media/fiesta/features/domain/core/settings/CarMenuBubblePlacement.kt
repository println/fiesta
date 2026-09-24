package proto.media.fiesta.features.domain.core.settings

object CarMenuBubblePlacement {

    fun toFraction(offset: Float, available: Int): Float =
        if (available <= 0) 0f else offset / available

    fun toOffset(fraction: Float, available: Int): Float = fraction * available

    fun fitsInside(left: Float, top: Float, width: Int, height: Int, parentWidth: Int, parentHeight: Int): Boolean =
        left >= 0f && top >= 0f && left + width <= parentWidth && top + height <= parentHeight
}
