package proto.media.fiesta.support.webviewex.document

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import proto.media.fiesta.support.webviewex.media.MediaElementEvent

internal class DocumentBridge(
    private val deliver: (String, DocumentStage) -> Unit,
    private val deliverMedia: (String, MediaElementEvent, Double) -> Unit
) {

    private val handler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onStage(documentId: String, stage: String) {
        val parsed = DocumentStage.fromJs(stage) ?: return
        handler.post { deliver(documentId, parsed) }
    }

    @JavascriptInterface
    fun onMediaEvent(documentId: String, event: String, positionSeconds: Double) {
        val parsed = MediaElementEvent.fromJs(event) ?: return
        handler.post { deliverMedia(documentId, parsed, positionSeconds) }
    }

    companion object {
        const val NAME = "webviewexdoc"
    }
}
