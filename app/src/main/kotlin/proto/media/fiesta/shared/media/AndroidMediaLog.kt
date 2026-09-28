package proto.media.fiesta.shared.media

import android.util.Log
import proto.media.fiesta.support.media.contract.MediaLog

object AndroidMediaLog : MediaLog {

    private const val TAG = "MediaServer"

    override fun debug(message: String) {
        Log.d(TAG, message)
    }

    override fun error(message: String, cause: Throwable?) {
        Log.e(TAG, message, cause)
    }
}
