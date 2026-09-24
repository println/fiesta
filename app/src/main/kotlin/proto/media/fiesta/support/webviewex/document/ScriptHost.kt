package proto.media.fiesta.support.webviewex.document

import android.util.Log

class ScriptHost internal constructor(
    private val lifecycle: DocumentLifecycle,
    private val evaluator: (String) -> Boolean
) {

    val documentId: String
        get() = lifecycle.documentId

    fun evaluate(script: String, forDocument: String = lifecycle.documentId): Boolean {
        if (forDocument != lifecycle.documentId) {
            Log.d(TAG, "script dropped: document $forDocument is gone")
            return false
        }
        return evaluator(script)
    }

    fun evaluateAt(stage: DocumentStage, script: String): Boolean =
        lifecycle.whenReached(lifecycle.documentId, stage) { evaluator(script) }

    private companion object {
        const val TAG = "WebViewEx"
    }
}
