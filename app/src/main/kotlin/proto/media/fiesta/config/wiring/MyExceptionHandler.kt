package proto.media.fiesta.config.wiring

import android.content.Context
import android.util.Log

class MyExceptionHandler(context: Context) : Thread.UncaughtExceptionHandler {
    private val context: Context = context.applicationContext
    private val previous: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        Log.e(TAG, "Uncaught exception in ${thread.name}", ex)
        previous?.uncaughtException(thread, ex)
    }

    companion object {
        private const val TAG = "FiestaCrash"
    }
}
