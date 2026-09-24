package proto.media.fiesta.support.system

import android.media.MediaDrm
import android.media.UnsupportedSchemeException
import android.util.Log
import java.util.UUID

object DrmDiagnostics {
    private const val TAG = "DrmDiag"
    private val WIDEVINE: UUID = UUID.fromString("edef8ba9-79d6-4ace-a3c8-27dcd51d21ed")

    @JvmStatic
    fun log() {
        var drm: MediaDrm? = null
        try {
            drm = MediaDrm(WIDEVINE)
            logProp(drm, "securityLevel")
            logProp(drm, "hdcpLevel")
            logProp(drm, "maxHdcpLevel")
            logProp(drm, "version")
            logProp(drm, "systemId")
            logProp(drm, "vendor")
        } catch (e: UnsupportedSchemeException) {
            Log.d(TAG, "Widevine not supported on this device/ROM", e)
        } catch (e: Exception) {
            Log.d(TAG, "Failed to query MediaDrm", e)
        } finally {
            drm?.release()
        }
    }

    private fun logProp(drm: MediaDrm, name: String) {
        try {
            Log.d(TAG, "$name = ${drm.getPropertyString(name)}")
        } catch (e: Exception) {
            Log.d(TAG, "$name = <unavailable: ${e.javaClass.simpleName}>")
        }
    }
}
