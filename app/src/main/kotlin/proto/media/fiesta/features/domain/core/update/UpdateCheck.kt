package proto.media.fiesta.features.domain.core.update

object UpdateCheck {

    fun shouldNotify(installedVersion: String, releaseTag: String): Boolean {
        val installed = AppVersion.parse(installedVersion) ?: return false
        val release = AppVersion.parse(releaseTag) ?: return false
        return release > installed
    }
}
