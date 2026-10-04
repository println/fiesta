package proto.media.fiezta.shared.media

import android.util.Log
import proto.media.fiezta.support.media.contract.MediaLog

object AndroidMediaLog : MediaLog {

    private const val TAG = "MediaServer"

    override fun debug(message: String) {
        Log.d(TAG, message)
    }

    override fun error(message: String, cause: Throwable?) {
        Log.e(TAG, message, cause)
    }
}
