package proto.media.fiesta.support.webviewex.document

import java.util.UUID

enum class DocumentStage(val jsName: String) {
    COMMITTED("committed"),
    DOM_READY("domReady"),
    LOADED("loaded"),
    IDLE("idle"),
    ROUTE_CHANGED("route");

    val isSticky: Boolean
        get() = this != ROUTE_CHANGED

    companion object {
        fun fromJs(name: String): DocumentStage? = values().firstOrNull { it.jsName == name }
    }
}

class DocumentLifecycle {

    var documentId: String = ""
        private set

    private val reached = mutableSetOf<DocumentStage>()
    private val waiting = mutableListOf<Pair<DocumentStage, () -> Unit>>()

    fun begin(): String {
        reached.clear()
        waiting.clear()
        documentId = UUID.randomUUID().toString()
        return documentId
    }

    fun hasReached(stage: DocumentStage): Boolean = stage in reached

    fun enter(documentId: String, stage: DocumentStage): Boolean {
        if (documentId != this.documentId) return false
        if (stage.isSticky && !reached.add(stage)) return false
        val ready = waiting.filter { it.first == stage }
        waiting.removeAll(ready)
        ready.forEach { it.second() }
        return true
    }

    fun whenReached(documentId: String, stage: DocumentStage, action: () -> Unit): Boolean {
        if (documentId != this.documentId) return false
        if (stage in reached) {
            action()
        } else {
            waiting.add(stage to action)
        }
        return true
    }
}
