package proto.media.fiezta.support.webviewex

import proto.media.fiezta.support.webviewex.navigation.WindowMode

data class WebViewExConfig(
    val name: String,
    val searchUrl: (String) -> String,
    val windowMode: WindowMode = WindowMode.SAME_TAB,
    val allowUniversalFileAccess: Boolean = false
) {
    companion object {
        fun phone(searchUrl: (String) -> String) = WebViewExConfig(
            name = "phone",
            searchUrl = searchUrl,
            allowUniversalFileAccess = true
        )
    }
}
