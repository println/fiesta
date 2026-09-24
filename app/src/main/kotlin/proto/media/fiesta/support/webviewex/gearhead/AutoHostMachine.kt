package proto.media.fiesta.support.webviewex.gearhead

enum class AutoHostState { NO_VIEW, ON_SCREEN, IN_BACKGROUND_WINDOW }

enum class AutoHostEvent { LEFT_WITHOUT_SCREEN, ATTACHED_TO_SCREEN, VIEW_DESTROYED }

enum class AutoHostEffect { NONE, HOST_IN_BACKGROUND_WINDOW, RELEASE_BACKGROUND_WINDOW }

data class AutoHostTransition(val state: AutoHostState, val effect: AutoHostEffect)

object AutoHostMachine {

    fun next(state: AutoHostState, event: AutoHostEvent): AutoHostTransition = when (event) {
        AutoHostEvent.LEFT_WITHOUT_SCREEN ->
            AutoHostTransition(AutoHostState.IN_BACKGROUND_WINDOW, AutoHostEffect.HOST_IN_BACKGROUND_WINDOW)
        AutoHostEvent.ATTACHED_TO_SCREEN ->
            AutoHostTransition(AutoHostState.ON_SCREEN, AutoHostEffect.NONE)
        AutoHostEvent.VIEW_DESTROYED -> AutoHostTransition(
            AutoHostState.NO_VIEW,
            if (state == AutoHostState.NO_VIEW) AutoHostEffect.NONE else AutoHostEffect.RELEASE_BACKGROUND_WINDOW
        )
    }
}
