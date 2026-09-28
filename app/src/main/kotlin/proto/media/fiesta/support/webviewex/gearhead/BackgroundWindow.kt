package proto.media.fiesta.support.webviewex.gearhead

import android.app.Presentation
import android.content.Context
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

internal class BackgroundWindow(context: Context, spec: DisplaySpec) {

    private val drainThread = HandlerThread(DRAIN_THREAD).apply { start() }
    private var reader = newReader(spec)
    private val display: VirtualDisplay = context.getSystemService(DisplayManager::class.java).createVirtualDisplay(
        DISPLAY_NAME, spec.width, spec.height, spec.densityDpi, reader.surface,
        DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
    )
    private val presentation = Presentation(context, display.display).apply {
        window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        show()
    }

    var spec: DisplaySpec = spec
        private set

    val windowContext: Context
        get() = presentation.context

    fun host(view: View) {
        (view.parent as? ViewGroup)?.removeView(view)
        presentation.setContentView(
            view, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
    }

    fun resize(target: DisplaySpec) {
        if (target == spec) return
        val previous = reader
        reader = newReader(target)
        display.resize(target.width, target.height, target.densityDpi)
        display.surface = reader.surface
        previous.close()
        spec = target
    }

    fun release() {
        presentation.dismiss()
        display.release()
        reader.close()
        drainThread.quitSafely()
    }

    private fun newReader(spec: DisplaySpec): ImageReader =
        ImageReader.newInstance(spec.width, spec.height, PixelFormat.RGBA_8888, MAX_IMAGES).apply {
            setOnImageAvailableListener({ it.acquireLatestImage()?.close() }, Handler(drainThread.looper))
        }

    private companion object {
        const val DISPLAY_NAME = "webviewex-gearhead-background"
        const val DRAIN_THREAD = "webviewex-gearhead-drain"
        const val MAX_IMAGES = 2
    }
}
