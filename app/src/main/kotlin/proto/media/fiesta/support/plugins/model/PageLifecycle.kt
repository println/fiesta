package proto.media.fiesta.support.plugins.model

enum class PageStage(val jsName: String) {
    COMMITTED("committed"),
    DOM_READY("domReady"),
    LOADED("loaded"),
    IDLE("idle"),
    ROUTE_CHANGED("route");

    companion object {
        fun fromJs(name: String): PageStage? = values().firstOrNull { it.jsName == name }
    }
}

enum class PageVisibility(val jsName: String) {
    FOREGROUND("foreground"),
    BACKGROUND("background")
}
