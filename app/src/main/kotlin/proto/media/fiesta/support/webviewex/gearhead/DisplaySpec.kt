package proto.media.fiesta.support.webviewex.gearhead

data class DisplaySpec(val width: Int, val height: Int, val densityDpi: Int) {

    fun shrunkTo(width: Int, height: Int) = copy(width = width, height = height)

    companion object {
        val CAR_DEFAULT = DisplaySpec(width = 800, height = 400, densityDpi = 160)
    }
}
