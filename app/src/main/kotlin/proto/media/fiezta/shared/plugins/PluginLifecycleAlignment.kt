package proto.media.fiezta.shared.plugins

import proto.media.fiezta.support.plugins.model.PageStage
import proto.media.fiezta.support.plugins.model.PageVisibility
import proto.media.fiezta.support.webviewex.document.DocumentStage
import proto.media.fiezta.support.webviewex.presentation.RenderMode

fun DocumentStage.toPageStage(): PageStage = when (this) {
    DocumentStage.COMMITTED -> PageStage.COMMITTED
    DocumentStage.DOM_READY -> PageStage.DOM_READY
    DocumentStage.LOADED -> PageStage.LOADED
    DocumentStage.IDLE -> PageStage.IDLE
    DocumentStage.ROUTE_CHANGED -> PageStage.ROUTE_CHANGED
}

fun RenderMode.toPageVisibility(): PageVisibility = when (this) {
    RenderMode.FOREGROUND -> PageVisibility.FOREGROUND
    RenderMode.BACKGROUND -> PageVisibility.BACKGROUND
}
