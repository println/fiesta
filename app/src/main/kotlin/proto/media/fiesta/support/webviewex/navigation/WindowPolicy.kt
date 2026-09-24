package proto.media.fiesta.support.webviewex.navigation

enum class WindowMode { SAME_TAB, DENY, NEW_TAB }

enum class WindowAction { OPEN_IN_SAME_TAB, DENY }

object WindowPolicy {

    fun decide(mode: WindowMode, isUserGesture: Boolean): WindowAction = when (mode) {
        WindowMode.DENY -> WindowAction.DENY
        WindowMode.SAME_TAB, WindowMode.NEW_TAB ->
            if (isUserGesture) WindowAction.OPEN_IN_SAME_TAB else WindowAction.DENY
    }
}
